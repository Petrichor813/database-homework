package com.volunteer.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.LoginRequest;
import com.volunteer.backend.dto.LoginResponse;
import com.volunteer.backend.dto.RegisterRequest;
import com.volunteer.backend.dto.RegisterResponse;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.utils.UserRole;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole(), token);
    }

    public RegisterResponse register(RegisterRequest request) {
        UserRole role = request.getRole() == null ? UserRole.USER : request.getRole();
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User(request.getUsername(), request.getPassword(), role);
        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }
}
