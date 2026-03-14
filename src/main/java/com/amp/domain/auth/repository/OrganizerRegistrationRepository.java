package com.amp.domain.auth.repository;

import com.amp.domain.auth.entity.OrganizerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRegistrationRepository extends JpaRepository<OrganizerRegistration, Long> {

    Optional<OrganizerRegistration> findByEmail(String email);

    boolean existsByEmail(String email);
}
