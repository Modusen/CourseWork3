package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repositories.NotificationTaskRepository;

import java.time.LocalDateTime;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public void create(Long chatId, String notification, LocalDateTime dateAndTime){
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setChatId(chatId);
        notificationTask.setNotification(notification);
        notificationTask.setDateTime(dateAndTime);
        notificationTaskRepository.save(notificationTask);
    }
}
