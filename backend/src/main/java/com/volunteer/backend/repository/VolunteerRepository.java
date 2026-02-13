package com.volunteer.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.utils.VolunteerStatus;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    List<Volunteer> findByDeletedFalse();

    Optional<Volunteer> findByIdAndDeletedFalse(Long id);

    Optional<Volunteer> findByUserIdAndDeletedFalse(Long userId);

    List<Volunteer> findByStatusAndDeletedFalse(VolunteerStatus status);

    List<Volunteer> findByStatusNotAndDeletedFalse(VolunteerStatus status);
}
