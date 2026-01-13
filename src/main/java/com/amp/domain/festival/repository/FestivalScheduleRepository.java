package com.amp.domain.festival.repository;

import com.amp.domain.festival.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule, Long> {
}
