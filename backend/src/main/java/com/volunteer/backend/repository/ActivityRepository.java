package com.volunteer.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.enums.ActivityType;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    // 这里要用 CONCAT，是因为直接写 %:keyword% 会变成字面量字符串，无法参数绑定
    @Query("SELECT a FROM Activity a WHERE "
            + "(:keyword IS NULL OR :keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a" +
            ".description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:type IS NULL OR a.type = :type) AND " + "(:status IS NULL OR a.status = :status) AND "
            + "(:date IS NULL OR :date = '' OR FUNCTION('DATE_FORMAT', a.startTime, '%Y-%m-%d') = :date)")
    // @formatter:off
    Page<Activity> findActivities(
        @Param("keyword") String keyword,
        @Param("type") ActivityType type,
        @Param("status") ActivityStatus status,
        @Param("date") String date,
        Pageable pageable
    );
    // @formatter:on

    @Query("SELECT a FROM Activity a WHERE "
            + "(:keyword IS NULL OR :keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a" +
            ".description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:type IS NULL OR a.type = :type) AND " + "(:status IS NULL OR a.status = :status) AND "
            + "(:date IS NULL OR :date = '' OR FUNCTION('DATE_FORMAT', a.startTime, '%Y-%m-%d') = :date) "
            + "ORDER BY CASE a.status " + "  WHEN 'RECRUITING' THEN 0 " + "  WHEN 'CONFIRMED' THEN 0 "
            + "  WHEN 'ONGOING' THEN 1 " + "  WHEN 'COMPLETED' THEN 2 " + "  WHEN 'CANCELLED' THEN 2 "
            + "  ELSE 3 END, a.startTime DESC")
    // @formatter:off
    Page<Activity> findActivitiesByStatusOrder(
        @Param("keyword") String keyword,
        @Param("type") ActivityType type,
        @Param("status") ActivityStatus status,
        @Param("date") String date,
        Pageable pageable
    );
    // @formatter:on

    @Query("SELECT a FROM Activity a WHERE " + "a.status IN ('RECRUITING', 'CONFIRMED') AND " + "a.maxParticipants > 0 "
            + "ORDER BY (a.curParticipants * 1.0 / a.maxParticipants) DESC, a.startTime ASC")
    List<Activity> findHotActivities(Pageable pageable);
}
