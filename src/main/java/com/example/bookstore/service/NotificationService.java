package com.example.bookstore.service;

import com.example.bookstore.dto.NotificationDTO;
import com.example.bookstore.entity.Notification;
import com.example.bookstore.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification save(Notification notification) {
        notification.setSentDate(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public Optional<Notification> findById(Integer id) {
        return notificationRepository.findById(id);
    }

    public void deleteById(Integer id) {
        notificationRepository.deleteById(id);
    }

    public List<NotificationDTO> findAllDTOs() {
        return notificationRepository.findAll()
                .stream()
                .map(n -> {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setNotificationId(n.getNotificationId());
                    dto.setTitle(n.getTitle());
                    dto.setContent(n.getContent());
                    dto.setSentDate(n.getSentDate());
                    return dto;
                }).toList();
    }
}