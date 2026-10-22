package com.portfolio.support.notification;

import com.portfolio.support.ticket.Ticket;
import com.portfolio.support.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notifications;
    private final ApplicationEventPublisher events;

    public void queue(User recipient, Ticket ticket, String type, String message) {
        Notification notification = notifications.save(new Notification(recipient, ticket, type, message));
        events.publishEvent(new NotificationQueued(notification.getId()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendStub(NotificationQueued event) {
        log.info("Notification stub queued: id={}", event.notificationId());
    }

    public record NotificationQueued(UUID notificationId) {}
}
