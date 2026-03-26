package com.eventflow.platform.controller;

import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventQueryRequest;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.EventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<PageResponse<EventSummaryDto>> getEvents(@ModelAttribute EventQueryRequest queryRequest) {
        return ApiResponse.success("OK", "Events fetched", eventService.getPublicEvents(queryRequest));
    }

    @GetMapping("/featured")
    public ApiResponse<List<EventSummaryDto>> getFeaturedEvents(@RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.success("OK", "Featured events fetched", eventService.getFeaturedEvents(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDetailDto> getEventDetail(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        return ApiResponse.success("OK", "Event detail fetched", eventService.getEventDetail(id, currentUserId));
    }
}
