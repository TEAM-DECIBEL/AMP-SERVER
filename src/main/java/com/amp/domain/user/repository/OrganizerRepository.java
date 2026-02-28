package com.amp.domain.user.repository;

import com.amp.domain.user.entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    Optional<Organizer> findByEmail(String email);
    boolean existsByOrganizerName(String organizerName);
}
