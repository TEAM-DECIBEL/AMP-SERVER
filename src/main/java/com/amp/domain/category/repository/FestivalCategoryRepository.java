package com.amp.domain.category.repository;

import com.amp.domain.category.entity.FestivalCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalCategoryRepository extends JpaRepository<FestivalCategory, Long> {
}
