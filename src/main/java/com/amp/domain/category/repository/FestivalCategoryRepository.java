package com.amp.domain.category.repository;

import com.amp.domain.category.entity.FestivalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FestivalCategoryRepository extends JpaRepository<FestivalCategory, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FestivalCategory c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.festival.id = :festivalId AND c.deletedAt IS NULL")
    void softDeleteByFestivalId(@Param("festivalId") Long festivalId);

    @Query("SELECT fc FROM FestivalCategory fc " +
            "WHERE fc.festival.id = :festivalId " +
            "AND fc.category.id = :categoryId " +
            "AND fc.isActive = true")
    Optional<FestivalCategory> findByMapping(
            @Param("festivalId") Long festivalId,
            @Param("categoryId") Long categoryId
    );
}
