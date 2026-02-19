package com.volunteer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

}
