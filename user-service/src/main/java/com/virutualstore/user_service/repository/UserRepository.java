package com.virutualstore.user_service.repository;

import com.virutualstore.user_service.entity.User;
import com.virutualstore.user_service.entity.UserRole;
import com.virutualstore.user_service.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    List<User> findByUserType(UserRole userRole);

    List<User> findByStatus(UserStatus status);

    List<User> findByUserTypeAndStatus(UserRole userRole, UserStatus status);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoff")
    List<User> findUnverifiedUsersCreatedBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.userId = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);
}