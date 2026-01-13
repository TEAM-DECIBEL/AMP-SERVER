package com.amp.domain.organizer.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    boolean existsByFestivalAndUser(Festival festival, User user);
}
