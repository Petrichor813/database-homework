package com.volunteer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.ExchangeItem;

@Repository
public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

}
