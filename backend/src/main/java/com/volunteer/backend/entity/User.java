/**
 * 用户实体类
 * <p>
 * 主要分三种用户：普通用户、志愿者用户和管理员用户
 * </p>
 * <ol>
 * <li>普通用户仅可以访问网站，不能报名活动</li>
 * <li>志愿者用户可以报名志愿活动等，但不能导入数据</li>
 * <li>管理员用户可以导入数据，也支持兼容志愿者</li>
 * </ol>
 */

package com.volunteer.backend.entity;

import java.time.LocalDateTime;

import com.volunteer.backend.utils.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    public User() {
        this.createTime = LocalDateTime.now();
    }

    public User(String username, String password, UserRole role) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
}
