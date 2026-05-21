package com.eventflow.platform.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Central hub for all business-level Prometheus metrics.
 * Services inject this bean instead of MeterRegistry directly,
 * so metric definitions stay in one place.
 */
@Service
public class BusinessMetricsService {

    private final MeterRegistry registry;

    public BusinessMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    // ── Booking ──────────────────────────────────────────────────────────────

    public void recordBookingAttempt(String result, String source) {
        counter("eventflow.booking.attempts",
                "result", result,
                "source", source)
                .increment();
    }

    public void recordBookingCancellation() {
        counter("eventflow.booking.cancellations").increment();
    }

    // ── Waitlist ─────────────────────────────────────────────────────────────

    public void recordWaitlistJoin() {
        counter("eventflow.waitlist.joins").increment();
    }

    public void recordWaitlistPromotion() {
        counter("eventflow.waitlist.promotions").increment();
    }

    // ── Check-in ─────────────────────────────────────────────────────────────

    public void recordCheckIn(String result) {
        counter("eventflow.checkin.attempts", "result", result).increment();
    }

    // ── Notification ─────────────────────────────────────────────────────────

    public void recordNotificationPublished(String notificationType) {
        counter("eventflow.notification.published",
                "notification_type", notificationType)
                .increment();
    }

    public void recordNotificationFailed(String notificationType) {
        counter("eventflow.notification.failed",
                "notification_type", notificationType)
                .increment();
    }

    // ── Event lifecycle ───────────────────────────────────────────────────────

    public void recordEventStatusTransition(String fromStatus, String toStatus) {
        counter("eventflow.event.status.transition",
                "from_status", fromStatus,
                "to_status", toStatus)
                .increment();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Counter counter(String name, String... tags) {
        return Counter.builder(name).tags(tags).register(registry);
    }
}
