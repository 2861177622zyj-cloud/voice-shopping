package com.zhaoyijin.voiceshopping.repository;

import com.zhaoyijin.voiceshopping.entity.UserProfileStaticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileStaticRepository extends JpaRepository<UserProfileStaticEntity, Long> {
}