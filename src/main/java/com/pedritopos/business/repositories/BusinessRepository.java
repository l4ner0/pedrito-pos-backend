package com.pedritopos.business.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pedritopos.business.domain.Business;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
}
