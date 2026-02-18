package com.volunteer.backend.entity;

import java.time.LocalDateTime;

import com.volunteer.backend.enums.ExchangeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exchange_record")
public class ExchangeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long volunteerId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long number; // 兑换的数量

    @Column(nullable = false)
    private Double totalPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime orderTime;

    // 处理时间，刚创建时可以为空
    private LocalDateTime processTime;

    // 处理备注
    @Column(length = 200)
    private String note;

    // 收货信息
    @Column(length = 200)
    private String recvInfo;

    public ExchangeRecord() {
        this.orderTime = LocalDateTime.now();
        this.status = ExchangeStatus.REVIEWING;
    }

    // @formatter:off
    public ExchangeRecord(
        Long volunteerId,
        Long itemId,
        Long number,
        Double totalPoints
    ) {
        this();

        if (number <= 0) {
            throw new IllegalArgumentException("兑换数量必须大于 0");
        }

        if (totalPoints <= 0) {
            throw new IllegalArgumentException("兑换积分必须大于 0");
        }

        this.volunteerId = volunteerId;
        this.itemId = itemId;
        this.number = number;
        this.totalPoints = totalPoints;
    }
    // @formatter:on

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        if (number <= 0) {
            throw new IllegalArgumentException("兑换数量必须大于 0");
        }
        this.number = number;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        if (totalPoints <= 0) {
            throw new IllegalArgumentException("兑换积分必须大于 0");
        }
        this.totalPoints = totalPoints;
    }

    public ExchangeStatus getStatus() {
        return status;
    }

    public void setStatus(ExchangeStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRecvInfo() {
        return recvInfo;
    }

    public void setRecvInfo(String recvInfo) {
        this.recvInfo = recvInfo;
    }

    // 是否可取消
    public boolean isCancellable() {
        return this.status == ExchangeStatus.REVIEWING;
    }

    // 是否终止
    // @formatter:off
    public boolean isFinalStatus() {
        return this.status == ExchangeStatus.COMPLETED
            || this.status == ExchangeStatus.CANCELLED
            || this.status == ExchangeStatus.REJECTED;
    }
    // @formatter:on

    // 设置状态为已完成
    public void markAsCompleted(String note) {
        this.status = ExchangeStatus.COMPLETED;
        this.processTime = LocalDateTime.now();
        this.note = note;
    }
}
