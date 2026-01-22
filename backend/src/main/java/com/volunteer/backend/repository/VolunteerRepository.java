package com.volunteer.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.utils.VolunteerStatus;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByUserId(Long userId);

    List<Volunteer> findByStatus(VolunteerStatus status);

    List<Volunteer> findByStatusNot(VolunteerStatus status);
}
