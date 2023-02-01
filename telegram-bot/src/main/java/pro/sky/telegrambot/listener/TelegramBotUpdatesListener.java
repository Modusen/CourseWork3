package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.repositories.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern PATTERN = Pattern.compile("([0-9.:\\s]{16})\\s([\\W]+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_HOURS = DateTimeFormatter.ofPattern("HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService,
                                      NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String receivedMessage = update.message().text();
            String userName = update.message().chat().firstName();
            Long chatId = update.message().chat().id();

            if ("/start".equals(update.message().text())) {
                responseStartCommand(chatId, userName);
            } else {
                LocalDateTime notificationDate;
                Matcher matcher = PATTERN.matcher(receivedMessage);
                if (matcher.matches() && (notificationDate = parse(matcher.group(1))) != null) {
                    String notificationText = matcher.group(2);
                    notificationTaskService.create(chatId, notificationText, notificationDate);
                    sendMessage(chatId, "Задача \"" + notificationText + "\" Запланирована на: " + notificationDate.format(DATE_TIME_FORMATTER));
                } else {
                    sendMessage(chatId, "Некорректный формат сообщения!");
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void responseStartCommand(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!!! To create a task send me a message like:\n " +
                "\n*DD.MM.YYYY HH.MM Text of the notification*";
        sendMessage(chatId, answer);
    }

    @Nullable
    private LocalDateTime parse(String notificationDate) {
        try {
            return LocalDateTime.parse(notificationDate, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        message.parseMode(ParseMode.Markdown);
        SendResponse sendResponse = telegramBot.execute(message);
        if (!sendResponse.isOk()) {
            logger.error(sendResponse.toString());
        }
    }

    @Scheduled (fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void sendTask() {
        notificationTaskRepository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(notificationTask -> {
                    sendMessage(notificationTask.getChatId(), notificationTask.getNotification() + " в " +
                            notificationTask.getDateTime().format(DATE_TIME_FORMATTER_HOURS));
                });
    }
}
