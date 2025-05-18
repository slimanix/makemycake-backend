package com.bootcamp.makemycake.services;

import com.bootcamp.makemycake.dto.RevenueAnalyticsResponse;
import com.bootcamp.makemycake.entities.Commande;
import com.bootcamp.makemycake.entities.StatutCommande;
import com.bootcamp.makemycake.repositories.CommandeRepository;
import com.bootcamp.makemycake.repositories.PatisserieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueAnalyticsService {

    private final CommandeRepository commandeRepository;
    private final PatisserieRepository patisserieRepository;

    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse getPatissierRevenue(Long patisserieId, String interval, int count) {
        // Validate parameters
        validateParameters(interval, count);
        
        // Verify patisserie exists
        if (!patisserieRepository.existsById(patisserieId)) {
            throw new IllegalArgumentException("Patisserie not found with ID: " + patisserieId);
        }

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(endDate, interval, count);
        
        String format = getDateFormat(interval);
        
        try {
            List<Object[]> revenueData = commandeRepository.findRevenueByPatisserieAndDateRange(
                patisserieId, format, startDate, endDate);

            List<RevenueAnalyticsResponse.RevenuePeriod> revenues = revenueData.stream()
                .map(row -> new RevenueAnalyticsResponse.RevenuePeriod(
                    (String) row[0],
                    ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());

            // Fill in missing periods with zero revenue
            List<RevenueAnalyticsResponse.RevenuePeriod> completeRevenues = 
                generateCompleteRevenuePeriods(startDate, endDate, interval, count, revenues);

            return new RevenueAnalyticsResponse(interval, count, completeRevenues);
        } catch (Exception e) {
            log.error("Error calculating revenue for patisserie {}: {}", patisserieId, e.getMessage(), e);
            throw new RuntimeException("Error calculating revenue data", e);
        }
    }

    private void validateParameters(String interval, int count) {
        if (count <= 0 || count > 365) {
            throw new IllegalArgumentException("Count must be between 1 and 365");
        }
        
        if (!List.of("day", "week", "month", "year").contains(interval.toLowerCase())) {
            throw new IllegalArgumentException("Interval must be one of: day, week, month, year");
        }
    }

    private LocalDateTime calculateStartDate(LocalDateTime endDate, String interval, int count) {
        return switch (interval.toLowerCase()) {
            case "day" -> endDate.minusDays(count - 1);
            case "week" -> endDate.minusWeeks(count - 1);
            case "month" -> endDate.minusMonths(count - 1);
            case "year" -> endDate.minusYears(count - 1);
            default -> throw new IllegalArgumentException("Invalid interval: " + interval);
        };
    }

    private String getDateFormat(String interval) {
        return switch (interval.toLowerCase()) {
            case "day" -> "%Y-%m-%d";
            case "week" -> "%Y-%u";
            case "month" -> "%Y-%m";
            case "year" -> "%Y";
            default -> throw new IllegalArgumentException("Invalid interval: " + interval);
        };
    }

    private List<RevenueAnalyticsResponse.RevenuePeriod> generateCompleteRevenuePeriods(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String interval,
            int count,
            List<RevenueAnalyticsResponse.RevenuePeriod> existingRevenues) {
        
        Map<String, Double> revenueMap = existingRevenues.stream()
            .collect(Collectors.toMap(
                RevenueAnalyticsResponse.RevenuePeriod::getPeriod,
                RevenueAnalyticsResponse.RevenuePeriod::getTotal
            ));

        List<RevenueAnalyticsResponse.RevenuePeriod> completeRevenues = new ArrayList<>();
        LocalDateTime currentDate = startDate;
        
        for (int i = 0; i < count; i++) {
            String period = formatPeriod(currentDate, interval);
            Double total = revenueMap.getOrDefault(period, 0.0);
            completeRevenues.add(new RevenueAnalyticsResponse.RevenuePeriod(period, total));
            
            currentDate = switch (interval.toLowerCase()) {
                case "day" -> currentDate.plusDays(1);
                case "week" -> currentDate.plusWeeks(1);
                case "month" -> currentDate.plusMonths(1);
                case "year" -> currentDate.plusYears(1);
                default -> throw new IllegalArgumentException("Invalid interval: " + interval);
            };
        }
        
        return completeRevenues;
    }

    private String formatPeriod(LocalDateTime date, String interval) {
        return switch (interval.toLowerCase()) {
            case "day" -> date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "week" -> date.format(DateTimeFormatter.ofPattern("yyyy-'W'ww"));
            case "month" -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "year" -> date.format(DateTimeFormatter.ofPattern("yyyy"));
            default -> throw new IllegalArgumentException("Invalid interval: " + interval);
        };
    }
} 