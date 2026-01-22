package com.volunteer.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.LoginRequest;
import com.volunteer.backend.dto.LoginResponse;
import com.volunteer.backend.dto.RegisterRequest;
import com.volunteer.backend.dto.RegisterResponse;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.utils.UserRole;
import com.volunteer.backend.utils.VolunteerStatus;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;

    public AuthService(UserRepository userRepository, VolunteerRepository volunteerRepository) {
        this.userRepository = userRepository;
        this.volunteerRepository = volunteerRepository;
    }

    public User authenticate(String username, String password, UserRole role) {
        User user = (role == null ? userRepository.findByUsername(username)
                : userRepository.findByUsernameAndRole(username, role))
                        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("密码错误");
        }
        return user;
    }

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request.getUsername(), request.getPassword(), request.getRole());
        String token = UUID.randomUUID().toString();
        Volunteer volunteer = volunteerRepository.findByUserId(user.getId()).orElse(null);
        VolunteerStatus volunteerStatus = volunteer != null ? volunteer.getStatus() : null;
        String phone = user.getPhone();
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole(), token, volunteerStatus, phone);
    }

    public RegisterResponse register(RegisterRequest request) {
        UserRole role = request.getRole() == null ? UserRole.USER : request.getRole();
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User(request.getUsername(), request.getPassword(), role, request.getPhone());
        User savedUser = userRepository.save(user);
        if (request.isRequestVolunteer() && role == UserRole.USER) {
            Volunteer volunteer = new Volunteer(request.getUsername(), request.getPhone(), savedUser.getId());
            volunteerRepository.save(volunteer);
        }
        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }
}
