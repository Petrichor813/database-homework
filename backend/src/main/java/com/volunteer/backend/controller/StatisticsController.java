package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.ActivityParticipationBubbleResponse;
import com.volunteer.backend.dto.ActivityTypeTrendResponse;
import com.volunteer.backend.dto.DashboardKPIResponse;
import com.volunteer.backend.dto.PointFlowSankeyResponse;
import com.volunteer.backend.dto.VolunteerActivityHeatmapResponse;
import com.volunteer.backend.dto.VolunteerGrowthRadarResponse;
import com.volunteer.backend.dto.VolunteerRetentionResponse;
import com.volunteer.backend.service.StatisticsService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/kpi")
    public ResponseEntity<DashboardKPIResponse> getDashboardKPI() {
        DashboardKPIResponse response = statisticsService.getDashboardKPI();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/volunteer-activity-heatmap")
    public ResponseEntity<VolunteerActivityHeatmapResponse> getVolunteerActivityHeatmap(
        @RequestParam(required = false) Integer year
    ) {
        VolunteerActivityHeatmapResponse response = statisticsService.getVolunteerActivityHeatmap(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/point-flow-sankey")
    public ResponseEntity<PointFlowSankeyResponse> getPointFlowSankey(
        @RequestParam(required = false) Integer year
    ) {
        PointFlowSankeyResponse response = statisticsService.getPointFlowSankey(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activity-participation-bubble")
    public ResponseEntity<ActivityParticipationBubbleResponse> getActivityParticipationBubble(
        @RequestParam(required = false) Integer year
    ) {
        ActivityParticipationBubbleResponse response = statisticsService.getActivityParticipationBubble(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activity-type-trend")
    public ResponseEntity<ActivityTypeTrendResponse> getActivityTypeTrend(
        @RequestParam(required = false) Integer year
    ) {
        ActivityTypeTrendResponse response = statisticsService.getActivityTypeTrend(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/volunteer-retention")
    public ResponseEntity<VolunteerRetentionResponse> getVolunteerRetention(
        @RequestParam(required = false) Integer year
    ) {
        VolunteerRetentionResponse response = statisticsService.getVolunteerRetention(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/volunteer-growth-radar")
    public ResponseEntity<VolunteerGrowthRadarResponse> getVolunteerGrowthRadar(
        @RequestParam(required = false) Long volunteerId
    ) {
        VolunteerGrowthRadarResponse response = statisticsService.getVolunteerGrowthRadar(volunteerId);
        return ResponseEntity.ok(response);
    }
}
