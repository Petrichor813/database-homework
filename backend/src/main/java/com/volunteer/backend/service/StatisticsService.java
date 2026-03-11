package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.ActivityParticipationBubble;
import com.volunteer.backend.dto.SankeyLink;
import com.volunteer.backend.dto.SankeyNode;
import com.volunteer.backend.dto.response.ActivityParticipationBubbleResponse;
import com.volunteer.backend.dto.response.ActivityTypeTrendResponse;
import com.volunteer.backend.dto.response.DashboardKPIResponse;
import com.volunteer.backend.dto.response.PointFlowSankeyResponse;
import com.volunteer.backend.dto.response.VolunteerActivityHeatmapResponse;
import com.volunteer.backend.dto.response.VolunteerGrowthRadarResponse;
import com.volunteer.backend.dto.response.VolunteerRetentionResponse;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.enums.ActivityType;
import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.repository.ActivityRepository;
import com.volunteer.backend.repository.PointChangeRecordRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class StatisticsService {
    private final SignupRecordRepository signupRecordRepository;
    private final ActivityRepository activityRepository;
    private final PointChangeRecordRepository pointChangeRecordRepository;
    private final VolunteerRepository volunteerRepository;

    // @formatter:off
    public StatisticsService(
        SignupRecordRepository signupRecordRepository,
        ActivityRepository activityRepository,
        PointChangeRecordRepository pointChangeRecordRepository,
        VolunteerRepository volunteerRepository
    ) {
        // @formatter:on
        this.signupRecordRepository = signupRecordRepository;
        this.activityRepository = activityRepository;
        this.pointChangeRecordRepository = pointChangeRecordRepository;
        this.volunteerRepository = volunteerRepository;
    }

    public DashboardKPIResponse getDashboardKPI() {
        List<SignupRecord> allSignups = signupRecordRepository.findAll();
        List<Activity> allActivities = activityRepository.findAll();
        List<PointChangeRecord> allPointRecords = pointChangeRecordRepository.findAll();

        Double totalServiceHours = 0.0;
        for (SignupRecord record : allSignups) {
            if (record.getActualHours() != null) {
                totalServiceHours += record.getActualHours();
            }
        }

        Integer totalActivities = 0;
        for (Activity activity : allActivities) {
            if (activity.getStatus() == ActivityStatus.COMPLETED) {
                totalActivities++;
            }
        }

        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        Integer activeVolunteers = 0;
        Map<Long, Boolean> volunteerActivityMap = new HashMap<>();
        for (SignupRecord record : allSignups) {
            // 用 actualHours 判断志愿者是否真正完成了活动
            if (record.getActualHours() != null && record.getActualHours() > 0) {
                // 检查活动完成时间是否在6个月内
                if (record.getSignupTime() != null && record.getSignupTime().isAfter(sixMonthsAgo)) {
                    if (!volunteerActivityMap.containsKey(record.getVolunteerId())) {
                        volunteerActivityMap.put(record.getVolunteerId(), true);
                        activeVolunteers++;
                    }
                }
            }
        }

        // 总积分发放包括活动发放、系统奖励以及管理员调整的正积分
        Double totalPointsReleased = 0.0;
        for (PointChangeRecord record : allPointRecords) {
            if (record.getChangePoints() != null && record.getChangePoints() > 0) {
                if (record.getChangeType() == PointChangeType.ACTIVITY_EARN
                        || record.getChangeType() == PointChangeType.SYSTEM_BONUS
                        || record.getChangeType() == PointChangeType.ADMIN_ADJUST) {
                    totalPointsReleased += record.getChangePoints();
                }
            }
        }

        return new DashboardKPIResponse(totalServiceHours, totalActivities, activeVolunteers, totalPointsReleased);
    }

    public VolunteerActivityHeatmapResponse getVolunteerActivityHeatmap(Integer year) {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i + "月");
        }

        List<String> weekdays = new ArrayList<>();
        weekdays.add("周一");
        weekdays.add("周二");
        weekdays.add("周三");
        weekdays.add("周四");
        weekdays.add("周五");
        weekdays.add("周六");
        weekdays.add("周日");

        List<List<Integer>> data = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                row.add(0);
            }
            data.add(row);
        }

        List<SignupRecord> allRecords = signupRecordRepository.findAll();
        for (SignupRecord record : allRecords) {
            if (record.getSignupTime() == null) {
                continue;
            }

            LocalDateTime signupTime = record.getSignupTime();
            int signupYear = signupTime.getYear();
            if (year != null && signupYear != year) {
                continue;
            }

            int month = signupTime.getMonthValue();
            int weekdayIndex = signupTime.getDayOfWeek().ordinal();

            int count = data.get(month - 1).get(weekdayIndex);
            data.get(month - 1).set(weekdayIndex, count + 1);
        }

        return new VolunteerActivityHeatmapResponse(months, weekdays, data);
    }

    public PointFlowSankeyResponse getPointFlowSankey(Integer year) {
        List<SankeyNode> nodes = new ArrayList<>();
        nodes.add(new SankeyNode("活动服务"));
        nodes.add(new SankeyNode("管理员添加"));
        nodes.add(new SankeyNode("系统奖励"));
        nodes.add(new SankeyNode("其他来源"));
        nodes.add(new SankeyNode("商品兑换"));
        nodes.add(new SankeyNode("积分过期"));
        nodes.add(new SankeyNode("管理员扣除"));
        nodes.add(new SankeyNode("其他去向"));

        Map<String, Double> sourceMap = new HashMap<>();
        sourceMap.put("活动服务", 0.0);
        sourceMap.put("管理员添加", 0.0);
        sourceMap.put("系统奖励", 0.0);
        sourceMap.put("其他来源", 0.0);

        Map<String, Double> targetMap = new HashMap<>();
        targetMap.put("商品兑换", 0.0);
        targetMap.put("积分过期", 0.0);
        targetMap.put("管理员扣除", 0.0);
        targetMap.put("其他去向", 0.0);

        List<PointChangeRecord> allRecords = pointChangeRecordRepository.findAll();
        for (PointChangeRecord record : allRecords) {
            if (record.getChangeTime() == null) {
                continue;
            }

            int recordYear = record.getChangeTime().getYear();
            if (year != null && recordYear != year) {
                continue;
            }

            if (record.getChangeType() == PointChangeType.ACTIVITY_EARN && record.getChangePoints() != null) {
                sourceMap.put("活动服务", sourceMap.get("活动服务") + record.getChangePoints());
            } else if (record.getChangeType() == PointChangeType.ADMIN_ADJUST && record.getChangePoints() != null) {
                if (record.getChangePoints() > 0) {
                    sourceMap.put("管理员添加", sourceMap.get("管理员添加") + record.getChangePoints());
                } else {
                    targetMap.put("管理员扣除", targetMap.get("管理员扣除") + Math.abs(record.getChangePoints()));
                }
            } else if (record.getChangeType() == PointChangeType.SYSTEM_BONUS && record.getChangePoints() != null) {
                sourceMap.put("系统奖励", sourceMap.get("系统奖励") + record.getChangePoints());
            } else if (record.getChangeType() == PointChangeType.EXCHANGE_USE && record.getChangePoints() != null) {
                targetMap.put("商品兑换", targetMap.get("商品兑换") + Math.abs(record.getChangePoints()));
            }
        }

        List<SankeyLink> links = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sourceMap.entrySet()) {
            if (entry.getValue() > 0) {
                links.add(new SankeyLink(entry.getKey(), "积分池", entry.getValue()));
            }
        }
        for (Map.Entry<String, Double> entry : targetMap.entrySet()) {
            if (entry.getValue() > 0) {
                links.add(new SankeyLink("积分池", entry.getKey(), entry.getValue()));
            }
        }

        List<SankeyNode> finalNodes = new ArrayList<>();
        finalNodes.add(new SankeyNode("积分池"));
        for (SankeyNode node : nodes) {
            boolean hasLink = false;
            for (SankeyLink link : links) {
                if (link.getSource().equals(node.getName()) || link.getTarget().equals(node.getName())) {
                    hasLink = true;
                    break;
                }
            }
            if (hasLink) {
                finalNodes.add(node);
            }
        }

        return new PointFlowSankeyResponse(finalNodes, links);
    }

    public ActivityParticipationBubbleResponse getActivityParticipationBubble(Integer year) {
        List<Activity> activities = activityRepository.findAll();
        List<SignupRecord> allRecords = signupRecordRepository.findAll();

        List<ActivityParticipationBubble> bubbles = new ArrayList<>();
        for (Activity activity : activities) {
            if (activity.getStartTime() == null) {
                continue;
            }

            int activityYear = activity.getStartTime().getYear();
            if (year != null && activityYear != year) {
                continue;
            }

            Integer participantCount = 0;
            Integer totalHours = 0;
            Double totalPoints = 0.0;

            for (SignupRecord record : allRecords) {
                if (record.getActivityId().equals(activity.getId())) {
                    participantCount++;
                    if (record.getActualHours() != null) {
                        totalHours += record.getActualHours();
                    }
                    if (record.getPoints() != null) {
                        totalPoints += record.getPoints();
                    }
                }
            }

            if (participantCount > 0) {
                String activityType = activity.getType() != null ? activity.getType().getDescription() : "未知";
                // @formatter:off
                bubbles.add(new ActivityParticipationBubble(
                    activity.getTitle(),
                    activity.getStartTime(),
                    activityType,
                    participantCount,
                    totalHours,
                    totalPoints
                ));
                // @formatter:on
            }
        }

        return new ActivityParticipationBubbleResponse(bubbles);
    }

    public ActivityTypeTrendResponse getActivityTypeTrend(Integer year) {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i + "月");
        }

        List<String> activityTypes = new ArrayList<>();
        for (ActivityType type : ActivityType.values()) {
            activityTypes.add(type.getDescription());
        }

        List<List<Integer>> data = new ArrayList<>();
        for (int i = 0; i < activityTypes.size(); i++) {
            List<Integer> typeData = new ArrayList<>();
            for (int j = 0; j < 12; j++) {
                typeData.add(0);
            }
            data.add(typeData);
        }

        List<Activity> allActivities = activityRepository.findAll();
        for (Activity activity : allActivities) {
            if (activity.getStartTime() == null) {
                continue;
            }

            int activityYear = activity.getStartTime().getYear();
            if (year != null && activityYear != year) {
                continue;
            }

            int month = activity.getStartTime().getMonthValue();
            String typeLabel = activity.getType() != null ? activity.getType().getDescription() : "未知";
            int typeIndex = activityTypes.indexOf(typeLabel);
            if (typeIndex >= 0) {
                int count = data.get(typeIndex).get(month - 1);
                data.get(typeIndex).set(month - 1, count + 1);
            }
        }

        return new ActivityTypeTrendResponse(months, activityTypes, data);
    }

    public VolunteerRetentionResponse getVolunteerRetention(Integer year) {
        // 如果志愿者在当月及后续月份都有活动，则在当月算作留存
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i + "月");
        }

        List<Double> retentionRates = new ArrayList<>();
        List<Integer> activeVolunteers = new ArrayList<>();
        List<Integer> retainedVolunteers = new ArrayList<>();
        List<Integer> lostVolunteers = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            retentionRates.add(0.0);
            activeVolunteers.add(0);
            retainedVolunteers.add(0);
            lostVolunteers.add(0);
        }

        List<SignupRecord> allRecords = signupRecordRepository.findAll();
        Map<Long, List<Integer>> volunteerMonthsMap = new HashMap<>();

        for (SignupRecord record : allRecords) {
            if (record.getSignupTime() == null) {
                continue;
            }

            int recordYear = record.getSignupTime().getYear();
            if (year != null && recordYear != year) {
                continue;
            }

            int month = record.getSignupTime().getMonthValue();
            Long volunteerId = record.getVolunteerId();

            if (!volunteerMonthsMap.containsKey(volunteerId)) {
                volunteerMonthsMap.put(volunteerId, new ArrayList<>());
            }
            volunteerMonthsMap.get(volunteerId).add(month);
        }

        for (int month = 1; month <= 12; month++) {
            int active = 0;
            int retained = 0;

            for (Map.Entry<Long, List<Integer>> entry : volunteerMonthsMap.entrySet()) {
                List<Integer> monthsParticipated = entry.getValue();
                if (monthsParticipated.contains(month)) {
                    active++;
                    boolean hasFutureActivity = false;
                    for (int futureMonth = month + 1; futureMonth <= 12; futureMonth++) {
                        if (monthsParticipated.contains(futureMonth)) {
                            hasFutureActivity = true;
                            break;
                        }
                    }
                    if (hasFutureActivity) {
                        retained++;
                    }
                }
            }

            int lost = active - retained;
            double retentionRate = active > 0 ? (retained * 100.0 / active) : 0.0;
            retentionRates.set(month - 1, Math.round(retentionRate * 10.0) / 10.0);
            activeVolunteers.set(month - 1, active);
            retainedVolunteers.set(month - 1, retained);
            lostVolunteers.set(month - 1, lost);
        }

        // @formatter:off
        return new VolunteerRetentionResponse(
            months,
            retentionRates,
            activeVolunteers,
            retainedVolunteers,
            lostVolunteers
        );
        // @formatter:on
    }

    private Integer calculateConsecutiveActiveMonths(Set<Integer> months) {
        if (months.isEmpty()) {
            return 0;
        }

        List<Integer> sortedMonths = new ArrayList<>(months);
        Collections.sort(sortedMonths);

        int maxConsecutive = 1;
        int curConsecutive = 1;

        for (int i = 1; i < sortedMonths.size(); i++) {
            int prev = sortedMonths.get(i - 1);
            int cur = sortedMonths.get(i);

            int prevYear = prev / 100;
            int prevMonth = prev % 100;
            int curYear = cur / 100;
            int curMonth = cur % 100;

            int monthDiff = (curYear - prevYear) * 12 + (curMonth - prevMonth);

            if (monthDiff == 1) {
                curConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, curConsecutive);
            } else {
                curConsecutive = 1;
            }
        }

        return maxConsecutive;
    }

    private Double calculateStandardDeviation(List<Double> values, Double mean) {
        if (values.isEmpty()) {
            return 0.0;
        }

        double sumSquared = 0.0;
        for (double v : values) {
            double diff = v - mean;
            sumSquared += diff * diff;
        }

        return Math.sqrt(sumSquared / values.size());
    }

    private Integer countImportantActivities(List<SignupRecord> records, ActivityRepository activityRepo) {
        int count = 0;
        for (SignupRecord record : records) {
            Activity activity = activityRepo.findById(record.getActivityId()).orElse(null);
            if (activity != null && activity.getPointsPerHour() != null && activity.getPointsPerHour() >= 5) {
                count++;
            }
        }
        return count;
    }

    private Double calculateActivityStability(Map<Integer, Integer> monthlyActivityCount) {
        if (monthlyActivityCount.isEmpty()) {
            return 0.0;
        }

        List<Integer> counts = new ArrayList<>(monthlyActivityCount.values());
        double mean = 0.0;
        if (counts != null && !counts.isEmpty()) {
            int sum = 0;
            for (Integer count : counts) {
                sum += count;
            }
            mean = (double) sum / counts.size();
        }

        if (mean == 0) {
            return 0.0;
        }

        double variance = 0.0;
        if (counts != null && !counts.isEmpty()) {
            double sumSquaredDiff = 0.0;
            for (Integer count : counts) {
                double diff = count - mean;
                sumSquaredDiff += diff * diff;
            }
            variance = sumSquaredDiff / counts.size();
        }

        double stdDev = Math.sqrt(variance);
        double cv = stdDev / mean;

        return Math.max(0, 1 - cv) * 100;
    }

    private Double calculateRecentActivityRatio(Map<Integer, Integer> monthlyActivityCount) {
        if (monthlyActivityCount.isEmpty()) {
            return 0.0;
        }

        int curYear = LocalDateTime.now().getYear();
        int curMonth = LocalDateTime.now().getMonthValue();

        int recentThreeMonths = 0; // 最近三个月活动次数
        int recentSixMonths = 0; // 最近六个月活动次数

        for (int i = 0; i < 3; i++) {
            int month = curMonth - i;
            int year = curYear;
            if (month <= 0) {
                month += 12;
                year--;
            }
            int monthKey = year * 100 + month;
            recentThreeMonths += monthlyActivityCount.getOrDefault(monthKey, 0);
        }

        for (int i = 0; i < 6; i++) {
            int month = curMonth - i;
            int year = curYear;
            if (month <= 0) {
                month += 12;
                year--;
            }
            int monthKey = year * 100 + month;
            recentSixMonths += monthlyActivityCount.getOrDefault(monthKey, 0);
        }

        return recentSixMonths > 0 ? (recentThreeMonths * 100.0 / recentSixMonths) : 0.0;
    }

    public VolunteerGrowthRadarResponse getVolunteerGrowthRadar(Long volunteerId) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(volunteerId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("志愿者被删除或不存在");
        }
        Volunteer volunteer = v.get();

        List<SignupRecord> records = signupRecordRepository.findByVolunteerId(volunteerId);

        Integer totalActivities = records.size();
        Integer totalHours = 0;
        Double totalPoints = 0.0;
        Integer completedActivities = 0;
        Set<String> activityTypes = new HashSet<>();
        Set<Integer> monthsParticipated = new HashSet<>();
        Map<Integer, Integer> monthlyActivityCount = new HashMap<>();
        Integer onTimeCompleted = 0;
        Integer earlySignup = 0;
        List<Double> serviceHoursList = new ArrayList<>();

        for (SignupRecord record : records) {
            if (record.getActualHours() != null && record.getActualHours() > 0) {
                totalHours += record.getActualHours();
                completedActivities++;
                serviceHoursList.add(record.getActualHours().doubleValue());
            }
            if (record.getPoints() != null) {
                totalPoints += record.getPoints();
            }

            Optional<Activity> a = activityRepository.findById(record.getActivityId());
            Activity activity = a != null ? a.get() : null;

            if (activity != null) {
                if (activity.getType() != null) {
                    activityTypes.add(activity.getType().getDescription());
                }

                if (record.getActualHours() != null && record.getActualHours() > 0) {
                    if (record.getVolunteerEndTime() != null && activity.getEndTime() != null) {
                        if (!record.getVolunteerEndTime().isAfter(activity.getEndTime())) {
                            onTimeCompleted++;
                        }
                    }
                }

                if (record.getSignupTime() != null && activity.getStartTime() != null) {
                    if (record.getSignupTime().isBefore(activity.getStartTime().minusDays(7))) {
                        earlySignup++;
                    }
                }
            }

            if (record.getSignupTime() != null) {
                int month = record.getSignupTime().getYear() * 100 + record.getSignupTime().getMonthValue(); // 这个数值表示年月，例如202308表示2023年8月
                monthsParticipated.add(month);
                monthlyActivityCount.put(month, monthlyActivityCount.getOrDefault(month, 0) + 1);
            }
        }

        Integer distinctActivityTypes = activityTypes.size();
        Integer totalActivityTypes = ActivityType.values().length;
        Integer monthsParticipatedCount = monthsParticipated.size();

        Integer consecutiveActiveMonths = calculateConsecutiveActiveMonths(monthsParticipated);

        Double activityCompletionRate = totalActivities > 0 ? (completedActivities * 100.0 / totalActivities) : 0.0;
        Double onTimeCompletionRate = completedActivities > 0 ? (onTimeCompleted * 100.0 / completedActivities) : 0.0;
        Double earlySignupRate = totalActivities > 0 ? (earlySignup * 100.0 / totalActivities) : 0.0;
        Double pointsPerHour = totalHours > 0 ? (totalPoints / totalHours) : 0.0;

        Double avgServiceHours = 0.0;
        if (!serviceHoursList.isEmpty()) {
            double sum = 0.0;
            for (Double hour : serviceHoursList) {
                sum += hour;
            }
            avgServiceHours = sum / serviceHoursList.size();
        }

        Double serviceHoursStdDev = calculateStandardDeviation(serviceHoursList, avgServiceHours);
        Double serviceStability = avgServiceHours > 0 ? Math.max(0, 1 - (serviceHoursStdDev / avgServiceHours)) * 100
                : 0.0;

        Integer importantActivities = countImportantActivities(records, activityRepository);
        Double importantActivityRatio = totalActivities > 0 ? (importantActivities * 100.0 / totalActivities) : 0.0;

        Double activityTypeDiversity = totalActivityTypes > 0 ? (distinctActivityTypes * 100.0 / totalActivityTypes)
                : 0.0;
        Double monthlyAvgActivity = monthsParticipatedCount > 0 ? (totalActivities * 1.0 / monthsParticipatedCount)
                : 0.0;

        Double activityStability = calculateActivityStability(monthlyActivityCount);
        Double recentActivityRatio = calculateRecentActivityRatio(monthlyActivityCount);

        Double activityParticipation = Math.min(100.0, 
            (completedActivities * 15.0) + 
            (activityTypeDiversity * 0.2) + 
            (monthlyAvgActivity * 10.0) + 
            (importantActivityRatio * 0.15));
        
        Double serviceQuality = Math.min(100.0,
            (totalHours * 1.5) + 
            (activityCompletionRate * 0.3) + 
            (onTimeCompletionRate * 0.2) + 
            (serviceStability * 0.1));
        
        Double continuity = Math.min(100.0,
            (monthsParticipatedCount * 8.0) + 
            (consecutiveActiveMonths * 15.0) + 
            (activityStability * 0.2) + 
            (recentActivityRatio * 0.25));
        
        Double initiative = Math.min(100.0,
            (earlySignupRate * 0.25) + 
            Math.min(100.0, pointsPerHour * 2.0) + 
            (activityCompletionRate * 0.25) + 
            (activityTypeDiversity * 0.15));

        // @formatter:off
        return new VolunteerGrowthRadarResponse(
            volunteer.getName(),
            volunteer.getPhone(),
            totalActivities,
            totalHours,
            totalPoints,
            activityParticipation,
            serviceQuality,
            continuity,
            initiative,
            completedActivities,
            distinctActivityTypes,
            monthsParticipatedCount,
            consecutiveActiveMonths,
            Math.round(activityCompletionRate * 10.0) / 10.0,
            Math.round(onTimeCompletionRate * 10.0) / 10.0,
            Math.round(earlySignupRate * 10.0) / 10.0,
            Math.round(pointsPerHour * 100.0) / 100.0
        );
        // @formatter:on
    }
}
