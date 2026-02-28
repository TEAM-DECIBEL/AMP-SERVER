package com.amp.domain.user.repository;

import com.amp.domain.user.entity.Audience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AudienceRepository extends JpaRepository<Audience, Long> {
    Optional<Audience> findByEmail(String email);
}
