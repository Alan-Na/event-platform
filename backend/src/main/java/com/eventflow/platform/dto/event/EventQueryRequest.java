package com.eventflow.platform.dto.event;

import lombok.Data;

@Data
public class EventQueryRequest {
    private String keyword;
    private String category;
    private String city;
    private String startDateFrom;
    private String startDateTo;
    private String sort = "START_ASC";
    private int page = 0;
    private int size = 9;
}
