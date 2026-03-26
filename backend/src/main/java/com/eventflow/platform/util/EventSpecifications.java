package com.eventflow.platform.util;

import com.eventflow.platform.entity.Category;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.enums.EventStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> statusIn(List<EventStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Event> statusEquals(EventStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> keywordContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern));
        };
    }

    public static Specification<Event> cityEquals(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) {
                return null;
            }
            return cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase());
        };
    }

    public static Specification<Event> categoryCodeEquals(String categoryCode) {
        return (root, query, cb) -> {
            if (categoryCode == null || categoryCode.isBlank()) {
                return null;
            }
            return cb.equal(root.join("category").get("code"), categoryCode.trim());
        };
    }

    public static Specification<Event> startDateFrom(String from) {
        return (root, query, cb) -> {
            if (from == null || from.isBlank()) {
                return null;
            }
            LocalDate date = LocalDate.parse(from);
            OffsetDateTime dateTime = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            return cb.greaterThanOrEqualTo(root.get("startTime"), dateTime);
        };
    }

    public static Specification<Event> startDateTo(String to) {
        return (root, query, cb) -> {
            if (to == null || to.isBlank()) {
                return null;
            }
            LocalDate date = LocalDate.parse(to).plusDays(1);
            OffsetDateTime dateTime = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            return cb.lessThan(root.get("startTime"), dateTime);
        };
    }
}
