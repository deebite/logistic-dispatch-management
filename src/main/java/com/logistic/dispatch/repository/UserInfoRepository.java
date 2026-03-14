package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {

    Optional<UserInfo> findByUsername(String username);

    boolean existsByUsername(String username);
}
