package com.eventflow.platform.repository;

import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.enums.BookingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findFirstByEventIdAndUserIdAndStatus(Long eventId, Long userId, BookingStatus status);

    @EntityGraph(attributePaths = {"event", "event.category", "waitlistEntry"})
    List<Booking> findAllByUserIdOrderByBookedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Booking> findAllByEventIdAndStatusOrderByBookedAtAsc(Long eventId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"event"})
    List<Booking> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Booking> findAllByEventIdAndStatus(Long eventId, BookingStatus status);

    long countByStatus(BookingStatus status);

    long countByUserIdAndStatus(Long userId, BookingStatus status);
}
