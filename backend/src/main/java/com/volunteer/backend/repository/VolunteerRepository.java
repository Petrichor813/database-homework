package com.volunteer.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.utils.VolunteerStatus;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Page<Volunteer> findByDeletedFalse(Pageable pageable);

    Optional<Volunteer> findByIdAndDeletedFalse(Long id);

    Optional<Volunteer> findByUserIdAndDeletedFalse(Long userId);

    Page<Volunteer> findByStatusAndDeletedFalse(VolunteerStatus status, Pageable pageable);

    Page<Volunteer> findByStatusNotAndDeletedFalse(VolunteerStatus status, Pageable pageable);
}
