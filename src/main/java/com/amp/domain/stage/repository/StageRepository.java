package com.amp.domain.stage.repository;

import com.amp.domain.stage.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface StageRepository extends JpaRepository<Stage, Long> {
}
