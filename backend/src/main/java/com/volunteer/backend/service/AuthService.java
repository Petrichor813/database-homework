package com.volunteer.backend.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.LoginRequest;
import com.volunteer.backend.dto.LoginResponse;
import com.volunteer.backend.dto.RegisterRequest;
import com.volunteer.backend.dto.RegisterResponse;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.UserRole;
import com.volunteer.backend.enums.VolunteerStatus;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;

    // @formatter:off
    public AuthService(
        UserRepository userRepository,
        VolunteerRepository volunteerRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.volunteerRepository = volunteerRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // @formatter:on

    public User authenticate(String username, String password, UserRole role) {
        // @formatter:off
        Optional<User> u = (role == null)
                            ? userRepository.findByUsernameAndDeletedFalse(username)
                            : userRepository.findByUsernameAndRoleAndDeletedFalse(username, role);
        // @formatter:on
        if (!u.isPresent()) {
            throw new IllegalArgumentException("用户不存在或该用户账号已注销");
        }
        User user = u.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        return user;
    }

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request.getUsername(), request.getPassword(), request.getRole());
        String token = UUID.randomUUID().toString();

        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(user.getId());
        Volunteer volunteer = v.isPresent() ? v.get() : null;

        VolunteerStatus volunteerStatus = (volunteer != null) ? volunteer.getStatus() : null;
        String phone = user.getPhone();

        // @formatter:off
        return new LoginResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            token,
            volunteerStatus,
            phone
        );
        // @formatter:on
    }

    public RegisterResponse register(RegisterRequest request) {
        UserRole role = (request.getRole() != null) ? request.getRole() : UserRole.USER;

        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), role, request.getPhone());
        User savedUser = userRepository.save(user);

        if (request.isRequestVolunteer() && role == UserRole.USER) {
            Volunteer volunteer = new Volunteer(request.getUsername(), request.getPhone(), savedUser.getId());
            volunteerRepository.save(volunteer);
        }

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }
}
