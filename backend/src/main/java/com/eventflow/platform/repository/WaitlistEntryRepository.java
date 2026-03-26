package com.eventflow.platform.repository;

import com.eventflow.platform.entity.WaitlistEntry;
import com.eventflow.platform.enums.WaitlistStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    Optional<WaitlistEntry> findFirstByEventIdAndUserIdAndStatus(Long eventId, Long userId, WaitlistStatus status);

    @Query("select coalesce(max(w.position), 0) from WaitlistEntry w where w.event.id = :eventId")
    Integer findMaxPositionByEventId(@Param("eventId") Long eventId);

    @EntityGraph(attributePaths = {"user"})
    Optional<WaitlistEntry> findFirstByEventIdAndStatusOrderByPositionAsc(Long eventId, WaitlistStatus status);

    @EntityGraph(attributePaths = {"event", "event.category"})
    List<WaitlistEntry> findAllByUserIdOrderByJoinedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<WaitlistEntry> findAllByEventIdAndStatusOrderByPositionAsc(Long eventId, WaitlistStatus status, Pageable pageable);

    long countByStatus(WaitlistStatus status);

    long countByUserIdAndStatus(Long userId, WaitlistStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<WaitlistEntry> findAllByEventIdAndStatus(Long eventId, WaitlistStatus status);
}
