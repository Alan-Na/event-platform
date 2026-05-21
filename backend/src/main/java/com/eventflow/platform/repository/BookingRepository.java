package com.eventflow.platform.repository;

import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.enums.BookingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = {"user", "ticketType"})
    @Query("select b from Booking b where b.event.id = :eventId and b.status in :statuses order by b.bookedAt asc")
    List<Booking> findAllByEventIdAndStatusIn(@Param("eventId") Long eventId,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("select count(b) from Booking b where b.event.id = :eventId and b.checkedInAt is not null and b.status = 'CONFIRMED'")
    long countCheckedInByEventId(@Param("eventId") Long eventId);

    Optional<Booking> findByEventIdAndConfirmationCode(Long eventId, String confirmationCode);
}
