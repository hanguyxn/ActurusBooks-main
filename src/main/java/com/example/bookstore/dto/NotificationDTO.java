package com.example.bookstore.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Integer notificationId;
    private String title;
    private String content;
    private LocalDateTime sentDate;
    // recipient có thể để null nếu không cần trả về
}
