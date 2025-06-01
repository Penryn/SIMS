package com.graduate.management.repository;

import com.graduate.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.loginAttempts = ?1 WHERE u.username = ?2")
    void updateLoginAttempts(Integer attempts, String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountNonLocked = ?1, u.lockedTime = ?2 WHERE u.username = ?3")
    void updateAccountLockStatus(Boolean locked, LocalDateTime lockedTime, String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = ?1, u.lastPasswordChangeTime = ?2, u.firstLogin = false WHERE u.username = ?3")
    void updatePassword(String password, LocalDateTime changeTime, String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginTime = ?1 WHERE u.username = ?2")
    void updateLastLoginTime(LocalDateTime loginTime, String username);
}
