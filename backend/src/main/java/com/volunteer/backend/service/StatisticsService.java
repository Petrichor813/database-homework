package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        Double totalPointsIssued = 0.0;
        for (PointChangeRecord record : allPointRecords) {
            if (record.getChangePoints() != null && record.getChangePoints() > 0) {
                if (record.getChangeType() == PointChangeType.ACTIVITY_EARN
                        || record.getChangeType() == PointChangeType.SYSTEM_BONUS
                        || record.getChangeType() == PointChangeType.ADMIN_ADJUST) {
                    totalPointsIssued += record.getChangePoints();
                }
            }
        }

        return new DashboardKPIResponse(totalServiceHours, totalActivities, activeVolunteers, totalPointsIssued);
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

        List<SignupRecord> allSignups = signupRecordRepository.findAll();
        for (SignupRecord record : allSignups) {
            if (record.getSignupTime() == null) {
                continue;
            }

            LocalDateTime signupTime = record.getSignupTime();
            int signupYear = signupTime.getYear();
            if (year != null && signupYear != year) {
                continue;
            }

            int month = signupTime.getMonthValue();
            int weekday = signupTime.getDayOfWeek().getValue();
            int weekdayIndex = weekday - 1;
            if (weekdayIndex < 0) {
                weekdayIndex = 6;
            }

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
        List<SignupRecord> allSignups = signupRecordRepository.findAll();

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

            for (SignupRecord signup : allSignups) {
                if (signup.getActivityId().equals(activity.getId())) {
                    participantCount++;
                    if (signup.getActualHours() != null) {
                        totalHours += signup.getActualHours();
                    }
                    if (signup.getPoints() != null) {
                        totalPoints += signup.getPoints();
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

        List<SignupRecord> allSignups = signupRecordRepository.findAll();
        Map<Long, List<Integer>> volunteerMonthsMap = new HashMap<>();

        for (SignupRecord record : allSignups) {
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
            int totalVolunteers = 0;
            int retainedCount = 0;

            for (Map.Entry<Long, List<Integer>> entry : volunteerMonthsMap.entrySet()) {
                List<Integer> monthsParticipated = entry.getValue();
                if (monthsParticipated.contains(month)) {
                    totalVolunteers++;
                    boolean hasFutureActivity = false;
                    for (int futureMonth = month + 1; futureMonth <= 12; futureMonth++) {
                        if (monthsParticipated.contains(futureMonth)) {
                            hasFutureActivity = true;
                            break;
                        }
                    }
                    if (hasFutureActivity) {
                        retainedCount++;
                    }
                }
            }

            int lostCount = totalVolunteers - retainedCount;
            double retentionRate = totalVolunteers > 0 ? (retainedCount * 100.0 / totalVolunteers) : 0.0;
            retentionRates.set(month - 1, Math.round(retentionRate * 10.0) / 10.0);
            activeVolunteers.set(month - 1, totalVolunteers);
            retainedVolunteers.set(month - 1, retainedCount);
            lostVolunteers.set(month - 1, lostCount);
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

    public VolunteerGrowthRadarResponse getVolunteerGrowthRadar(Long volunteerId) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(volunteerId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("志愿者被删除或不存在");
        }
        Volunteer volunteer = v.get();

        List<SignupRecord> signups = signupRecordRepository.findByVolunteerId(volunteerId);

        Integer totalActivities = signups.size();
        Integer totalHours = 0;
        Double totalPoints = 0.0;

        for (SignupRecord signup : signups) {
            if (signup.getActualHours() != null) {
                totalHours += signup.getActualHours();
            }
            if (signup.getPoints() != null) {
                totalPoints += signup.getPoints();
            }
        }

        Integer activityParticipation = Math.min(100, totalActivities * 5);
        Integer serviceQuality = Math.min(100, totalHours * 2);
        Integer continuity = Math.min(100, totalActivities * 3);
        Integer initiative = Math.min(100, (int) (totalPoints / 10));

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
            initiative
        );
        // @formatter:on
    }
}
