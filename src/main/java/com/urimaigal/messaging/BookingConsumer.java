package com.urimaigal.messaging;

import com.urimaigal.dto.BookingEvent;
import com.urimaigal.repository.ConsultationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * JMS Consumer — listens to Artemis queues using @JmsListener.
 * Processes booking events and sends notifications.
 * Constructor injection (no Lombok).
 */
@Component
public class BookingConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingConsumer.class);

    private final ConsultationRepository consultationRepository;

    public BookingConsumer(ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    /**
     * Consumes from the booking queue.
     * Handles post-booking logic: confirmation, indexing, analytics, etc.
     */
    @JmsListener(destination = "${app.jms.booking-queue}",
                 containerFactory = "jmsListenerContainerFactory")
    public void processBookingEvent(BookingEvent event) {
        log.info("Received booking event [{}] for booking '{}' (lawyer: {}, user: {})",
                event.getEventType(), event.getBookingId(), event.getLawyerId(), event.getUserId());

        try {
            switch (event.getEventType()) {
                case "BOOKING_CREATED" -> handleBookingCreated(event);
                case "BOOKING_CANCELLED" -> handleBookingCancelled(event);
                default -> log.warn("Unknown booking event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing booking event '{}': {}", event.getBookingId(), e.getMessage(), e);
            // In production: send to DLQ or retry topic
            throw e; // re-throw so Artemis can redeliver (session is transacted)
        }
    }

    /**
     * Consumes from the notification queue.
     * In production: triggers email/SMS via a mail service or SNS.
     */
    @JmsListener(destination = "${app.jms.notification-queue}",
                 containerFactory = "jmsListenerContainerFactory")
    public void processNotification(BookingEvent event) {
        log.info("Processing notification for booking '{}' -> user email: '{}'",
                event.getBookingId(), event.getUserEmail());

        // Simulate sending confirmation email
        sendConfirmationEmail(event);
    }

    // ==========================================
    // Private handlers
    // ==========================================

    private void handleBookingCreated(BookingEvent event) {
        log.info("Booking CREATED: id={}, lawyer={}, date={} {}",
                event.getBookingId(), event.getLawyerName(), event.getDate(), event.getTime());
        // Additional post-processing:
        // - Update lawyer calendar/availability
        // - Trigger analytics event
        // - Sync with external calendar
    }

    private void handleBookingCancelled(BookingEvent event) {
        log.info("Booking CANCELLED: id={}, restoring lawyer availability if needed",
                event.getBookingId());
        // Update status (already done in ConsultationService), handle refunds, etc.
    }

    private void sendConfirmationEmail(BookingEvent event) {
        // In production, inject a MailSender or AWS SES client here.
        // For now, log the confirmation details.
        log.info("""
                [EMAIL SIMULATION]
                To: {}
                Subject: Consultation Confirmed — {}
                Body: Your consultation with {} is confirmed for {} at {} ({} mode).
                      Fee: ₹{}
                      Booking ID: {}
                """,
                event.getUserEmail(),
                event.getLawyerName(),
                event.getLawyerName(),
                event.getDate(),
                event.getTime(),
                event.getMode(),
                event.getFee(),
                event.getBookingId());
    }
}
