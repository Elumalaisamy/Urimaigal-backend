package com.urimaigal.messaging;

import com.urimaigal.dto.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * JMS Producer — publishes events to Artemis queues using JmsTemplate.
 * Constructor injection used throughout (no Lombok).
 */
@Component
public class BookingProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingProducer.class);

    private final JmsTemplate jmsTemplate;
    private final String bookingQueue;
    private final String notificationQueue;

    public BookingProducer(JmsTemplate jmsTemplate,
                           @Value("${app.jms.booking-queue}") String bookingQueue,
                           @Value("${app.jms.notification-queue}") String notificationQueue) {
        this.jmsTemplate = jmsTemplate;
        this.bookingQueue = bookingQueue;
        this.notificationQueue = notificationQueue;
    }

    /**
     * Send a booking event to the booking queue for processing.
     */
    public void sendBookingEvent(BookingEvent event) {
        try {
            jmsTemplate.convertAndSend(bookingQueue, event);
            log.info("Sent booking event [{}] for booking '{}' to queue '{}'",
                    event.getEventType(), event.getBookingId(), bookingQueue);
        } catch (Exception e) {
            log.error("Failed to send booking event for '{}': {}", event.getBookingId(), e.getMessage(), e);
        }
    }

    /**
     * Send a notification event (e.g., email confirmation) to the notification queue.
     */
    public void sendNotification(BookingEvent event) {
        try {
            jmsTemplate.convertAndSend(notificationQueue, event);
            log.info("Sent notification event for booking '{}' to queue '{}'",
                    event.getBookingId(), notificationQueue);
        } catch (Exception e) {
            log.error("Failed to send notification for '{}': {}", event.getBookingId(), e.getMessage(), e);
        }
    }
}
