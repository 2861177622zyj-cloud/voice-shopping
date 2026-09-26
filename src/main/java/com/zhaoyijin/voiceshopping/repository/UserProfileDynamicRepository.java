package com.zhaoyijin.voiceshopping.repository;

import com.zhaoyijin.voiceshopping.entity.UserProfileDynamicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileDynamicRepository extends JpaRepository<UserProfileDynamicEntity, Long> {
}