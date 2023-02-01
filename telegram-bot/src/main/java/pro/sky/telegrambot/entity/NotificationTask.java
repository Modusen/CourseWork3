package pro.sky.telegrambot.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String notification;
    private LocalDateTime dateTime;

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getNotification() {
        return notification;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public void setDateTime(LocalDateTime dateAndTime) {
        this.dateTime = dateAndTime;
    }
}
