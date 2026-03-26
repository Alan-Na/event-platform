package com.eventflow.platform.repository;

import com.eventflow.platform.entity.Event;
import com.eventflow.platform.enums.EventStatus;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("select e from Event e left join fetch e.category where e.id = :id")
    Optional<Event> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e left join fetch e.category where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    @Query("select e from Event e left join fetch e.category where e.featured = true and e.status = com.eventflow.platform.enums.EventStatus.PUBLISHED and e.registrationDeadline > :now order by e.startTime asc")
    List<Event> findFeaturedPublished(@Param("now") OffsetDateTime now, Pageable pageable);

    @Modifying
    @Query("update Event e set e.status = :closed where e.status = :published and e.registrationDeadline < :now")
    int closeExpiredPublishedEvents(@Param("published") EventStatus published, @Param("closed") EventStatus closed, @Param("now") OffsetDateTime now);

    @EntityGraph(attributePaths = "category")
    List<Event> findAllByStatusIn(List<EventStatus> statuses, Pageable pageable);

    boolean existsBySlug(String slug);

    long countByStatus(EventStatus status);

    long countByStatusAndStartTimeAfter(EventStatus status, OffsetDateTime startTime);
}
