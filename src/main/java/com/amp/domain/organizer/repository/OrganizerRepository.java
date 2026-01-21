package com.amp.domain.organizer.repository;

import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    Boolean existsByUser(User user);

    Optional<Organizer> findByUserId(Long userId);

}
