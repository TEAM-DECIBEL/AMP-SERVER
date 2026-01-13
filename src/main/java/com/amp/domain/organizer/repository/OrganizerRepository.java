package com.amp.domain.organizer.repository;

import com.amp.domain.organizer.entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
}
