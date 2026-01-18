package com.volunteer.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.LoginRequest;
import com.volunteer.backend.dto.LoginResponse;
import com.volunteer.backend.dto.RegisterRequest;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.utils.UserRole;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request.getUsername(), request.getPassword(), request.getRole());
        String token = UUID.randomUUID().toString();
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole(), token);
    }

    public User register(RegisterRequest request) {
        UserRole role = request.getRole() == null ? UserRole.USER : request.getRole();
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User(request.getUsername(), request.getPassword(), role);
        return userRepository.save(user);
    }

    public User authenticate(String username, String password, UserRole role) {
        UserRole queryRole = role == null ? UserRole.USER : role;
        User user = userRepository.findByUsernameAndRole(username, queryRole)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("密码错误");
        }
        return user;
    }
}
