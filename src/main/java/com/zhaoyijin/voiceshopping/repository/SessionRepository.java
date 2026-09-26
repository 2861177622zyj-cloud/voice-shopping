package com.zhaoyijin.voiceshopping.repository;

import com.zhaoyijin.voiceshopping.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
}