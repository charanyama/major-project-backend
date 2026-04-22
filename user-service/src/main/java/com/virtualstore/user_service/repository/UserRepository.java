package com.virtualstore.user_service.repository;

import com.virtualstore.user_service.entity.Role;
import com.virtualstore.user_service.entity.Status;
import com.virtualstore.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Historical MongoDB repository kept for reference:
 *
 * // @Repository
 * // public interface UserRepository extends MongoRepository<User, String> {
 * //
 * //     Optional<User> findByEmail(String email);
 * //
 * //     Optional<User> findByPasswordResetToken(String token);
 * //
 * //     boolean existsByEmail(String email);
 * //
 * //     List<User> findAllByDeletedFalse();
 * //
 * //     List<User> findAllByRoleAndDeletedFalse(Role role);
 * //
 * //     List<User> findAllByStatusAndDeletedFalse(Status status);
 * // }
 */

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    List<User> findAllByDeletedFalse();

    List<User> findAllByRoleAndDeletedFalse(Role role);

    List<User> findAllByStatusAndDeletedFalse(Status status);
}
