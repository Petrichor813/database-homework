package com.volunteer.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.ActivityResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.repository.ActivityRepository;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    private int statusOrder(ActivityStatus status) {
        return switch (status) {
        case RECRUITING, CONFIRMED -> 0;
        case ONGOING -> 1;
        case COMPLETED, CANCELLED -> 2;
        default -> 3;
        };
    }

    private Comparator<Activity> buildComparator(String sort) {
        if ("status".equalsIgnoreCase(sort)) {
            return Comparator.comparingInt((Activity activity) -> statusOrder(activity.getStatus()))
                    .thenComparing(Activity::getStartTime, Comparator.reverseOrder());
        }
        return Comparator.comparing(Activity::getStartTime, Comparator.reverseOrder());
    }

    private void refreshActivityStatus(Activity activity, LocalDateTime now) {
        if (activity.getStatus() == ActivityStatus.CANCELLED) {
            return;
        }

        if (now.isAfter(activity.getEndTime())) {
            activity.setStatus(ActivityStatus.COMPLETED);
            return;
        }

        if (now.isAfter(activity.getStartTime()) || now.isEqual(activity.getStartTime())) {
            activity.setStatus(ActivityStatus.ONGOING);
        } else if (activity.getStatus() == ActivityStatus.COMPLETED) {
            activity.setStatus(ActivityStatus.RECRUITING);
        }
    }

    private ActivityResponse mapToResponse(Activity activity) {
        // @formatter:off
        return new ActivityResponse(
            activity.getId(),
            activity.getTitle(),
            activity.getDescription(),
            activity.getType(),
            activity.getLocation(),
            activity.getStartTime(),
            activity.getEndTime(),
            activity.getStatus(),
            activity.getPointsPerHour(),
            activity.getMaxParticipants(),
            activity.getCurParticipants()
        );
        // @formatter:on
    }

    // @formatter:off
    public PageResponse<ActivityResponse> getActivities(
        int page,
        int size,
        String keyword,
        String type,
        String status,
        String date,
        String sort
    ) {
        // @formatter:on
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        List<Activity> allActivities = new ArrayList<>(activityRepository.findAll());
        LocalDateTime now = LocalDateTime.now();

        List<Activity> filtered = allActivities.stream().filter(activity -> {
            refreshActivityStatus(activity, now);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
                String title = activity.getTitle() == null ? "" : activity.getTitle().toLowerCase(Locale.ROOT);
                String description = activity.getDescription() == null ? ""
                        : activity.getDescription().toLowerCase(Locale.ROOT);
                if (!title.contains(lowerKeyword) && !description.contains(lowerKeyword)) {
                    return false;
                }
            }

            if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
                if (!activity.getType().name().equalsIgnoreCase(type)) {
                    return false;
                }
            }

            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                if (!activity.getStatus().name().equalsIgnoreCase(status)) {
                    return false;
                }
            }

            if (date != null && !date.isBlank()) {
                LocalDate filterDate = LocalDate.parse(date);
                LocalDate activityDate = activity.getStartTime().toLocalDate();
                if (!filterDate.equals(activityDate)) {
                    return false;
                }
            }

            return true;
        }).toList();

        Comparator<Activity> comparator = buildComparator(sort);
        List<ActivityResponse> content = filtered.stream().sorted(comparator).map(this::mapToResponse).toList();

        int totalElements = content.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        if (start >= totalElements) {
            return new PageResponse<>(new ArrayList<>(), page, size, totalElements, totalPages);
        }
        int end = Math.min(start + size, totalElements);
        List<ActivityResponse> pagedContent = content.subList(start, end);

        return new PageResponse<>(pagedContent, page, size, totalElements, totalPages);
    }
}
