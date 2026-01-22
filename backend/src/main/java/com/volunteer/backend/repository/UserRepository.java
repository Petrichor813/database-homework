package com.volunteer.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.User;
import com.volunteer.backend.utils.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(UserRole role);

    Optional<User> findByUsernameAndRole(String username, UserRole role);

    @Query("SELECT u FROM User u WHERE u.id = (SELECT v.userId FROM Volunteer v WHERE v.id = :volunteerId)")
    Optional<User> findByVolunteerId(@Param("volunteerId") Long volunteerId);

    List<User> findByIdIn(List<Long> ids);

    List<User> findByUsernameIn(List<String> usernames);
}
