package com.ssafy.logoserver.domain.user.repository;

import com.ssafy.logoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsById(String id);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByUuid(Long uuid);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByProviderAndProviderId(String provider, String providerId);
}