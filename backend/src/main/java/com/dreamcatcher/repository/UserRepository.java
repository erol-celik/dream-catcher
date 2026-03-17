package com.dreamcatcher.repository;

import com.dreamcatcher.entity.User;
import com.dreamcatcher.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 * Supports lookup by email, guest token, and auth provider credentials.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGuestToken(String guestToken);

    Optional<User> findByAuthProviderIdAndAuthProvider(String authProviderId, AuthProvider authProvider);

    boolean existsByEmail(String email);

}
