package com.bootcamp.makemycake.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {
    private String interval;
    private int count;
    private List<RevenuePeriod> revenues;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenuePeriod {
        private String period;
        private Double total;
    }
} 