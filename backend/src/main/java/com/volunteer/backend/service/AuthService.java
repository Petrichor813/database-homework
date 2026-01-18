package com.volunteer.backend.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String password, UserRole role) {
        UserRole queryRole = (role == null) ? UserRole.USER : role;
        User user = userRepository.findByUsernameAndRole(username, queryRole)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("密码错误");
        }
        return user;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = authenticate(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getRole());
        String token = UUID.randomUUID().toString();
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole(), token);
    }

    public User register(RegisterRequest registerRequest) {
        UserRole role = registerRequest.getRole() == null ? UserRole.USER : registerRequest.getRole();
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User(registerRequest.getUsername(), registerRequest.getPassword(), role);
        return userRepository.save(user);
    }
}
