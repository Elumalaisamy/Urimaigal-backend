package com.urimaigal.service;

import com.urimaigal.dto.BookingEvent;
import com.urimaigal.dto.BookingRequest;
import com.urimaigal.exception.ResourceNotFoundException;
import com.urimaigal.messaging.BookingProducer;
import com.urimaigal.model.Consultation;
import com.urimaigal.model.Lawyer;
import com.urimaigal.model.User;
import com.urimaigal.repository.ConsultationRepository;
import com.urimaigal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConsultationService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationService.class);

    private final ConsultationRepository consultationRepository;
    private final LawyerService lawyerService;
    private final UserRepository userRepository;
    private final BookingProducer bookingProducer;

    public ConsultationService(ConsultationRepository consultationRepository,
                               LawyerService lawyerService,
                               UserRepository userRepository,
                               BookingProducer bookingProducer) {
        this.consultationRepository = consultationRepository;
        this.lawyerService = lawyerService;
        this.userRepository = userRepository;
        this.bookingProducer = bookingProducer;
    }

    public List<Consultation> getClientConsultations(String clientId) {
        return consultationRepository.findByClientId(clientId);
    }

    public List<Consultation> getAdvocateConsultations(String advocateId) {
        return consultationRepository.findByAdvocateId(advocateId);
    }

    public Consultation getConsultationById(String id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + id));
    }

    /**
     * Book a consultation:
     * 1. Validate lawyer exists.
     * 2. Persist to MySQL.
     * 3. Publish JMS events (booking + notification queues).
     */
    public Consultation bookConsultation(String userId, BookingRequest request) {
        // 1. Fetch lawyer — throws 404 if not found
        Lawyer lawyer = lawyerService.getLawyerById(request.getLawyerId());

        // 2. Fetch user for email notification
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 3. Build consultation entity
        Consultation consultation = new Consultation();
        consultation.setId("bk-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        consultation.setClientId(userId);
        consultation.setAdvocateId(lawyer.getId());
        consultation.setLawyerName(lawyer.getName());
        consultation.setConsultationDate(LocalDate.parse(request.getDate()));
        consultation.setConsultationTime(request.getTime());
        consultation.setMode(request.getMode());
        consultation.setStatus("scheduled");
        consultation.setFee(lawyer.getConsultationFee());
        consultation.setNotes(request.getNotes());

        // 4. Persist
        consultationRepository.save(consultation);
        log.info("Consultation booked: id={}, user={}, lawyer={}",
                consultation.getId(), userId, lawyer.getName());

        // 5. Publish to JMS queues (async processing + notification)
        BookingEvent event = new BookingEvent(
                consultation.getId(), userId, lawyer.getId(), lawyer.getName(),
                user.getEmail(), request.getDate(), request.getTime(),
                request.getMode(), lawyer.getConsultationFee(), "BOOKING_CREATED");

        bookingProducer.sendBookingEvent(event);
        bookingProducer.sendNotification(event);

        return consultation;
    }

    /**
     * Cancel a consultation — updates DB and publishes a cancel event.
     */
    public Consultation cancelConsultation(String consultationId, String advocateId) {
        Consultation consultation = getConsultationById(consultationId);

        if (!consultation.getAdvocateId().equals(advocateId)) {
            throw new SecurityException("You are not authorised to cancel this consultation");
        }
        if ("cancelled".equals(consultation.getStatus())) {
            throw new IllegalStateException("Consultation is already cancelled");
        }

        consultationRepository.updateStatus(consultationId, "cancelled");
        consultation.setStatus("cancelled");

        // Notify via JMS
        BookingEvent event = new BookingEvent(
                consultationId, consultation.getClientId(), consultation.getAdvocateId(), consultation.getLawyerName(),
                null, consultation.getConsultationDate().toString(), consultation.getConsultationTime(),
                consultation.getMode(), consultation.getFee(), "BOOKING_CANCELLED");
        bookingProducer.sendBookingEvent(event);

        log.info("Consultation cancelled: id={}", consultationId);
        return consultation;
    }

    public Consultation completeConsultation(String consultationId) {
        Consultation consultation = getConsultationById(consultationId);
        consultationRepository.updateStatus(consultationId, "completed");
        consultation.setStatus("completed");
        return consultation;
    }

    public Map<String, Object> getAdvocateEarnings(String advocateId) {
        List<Consultation> completedConsultations = consultationRepository.findByAdvocateIdAndStatus(advocateId, "completed");
        BigDecimal totalEarnings = completedConsultations.stream()
                .map(Consultation::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> earnings = new HashMap<>();
        earnings.put("totalEarnings", totalEarnings);
        earnings.put("completedConsultations", completedConsultations.size());
        return earnings;
    }

    public Map<String, Object> getAdvocateAnalytics(String advocateId) {
        List<Consultation> allConsultations = consultationRepository.findByAdvocateId(advocateId);
        long totalConsultations = allConsultations.size();
        long completedConsultations = allConsultations.stream()
                .filter(c -> "completed".equals(c.getStatus()))
                .count();
        long cancelledConsultations = allConsultations.stream()
                .filter(c -> "cancelled".equals(c.getStatus()))
                .count();

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalConsultations", totalConsultations);
        analytics.put("completedConsultations", completedConsultations);
        analytics.put("cancelledConsultations", cancelledConsultations);
        analytics.put("completionRate", totalConsultations > 0 ? (double) completedConsultations / totalConsultations * 100 : 0);
        return analytics;
    }
}
