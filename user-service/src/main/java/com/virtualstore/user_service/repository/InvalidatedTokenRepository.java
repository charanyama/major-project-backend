package com.virtualstore.user_service.repository;

import com.virtualstore.user_service.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    /** Used by JwtAuthenticationFilter to check if a token has been blocklisted */
    boolean existsByJti(String jti);
}
