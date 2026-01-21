package com.amp.domain.notice.repository;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<Notice> findById(Long id);

    void deleteAllByFestivalAndFestivalCategory(Festival festival, FestivalCategory festivalCategory);

    Page<Notice> findAllByFestival(Festival festival, Pageable pageable);

    @Query("SELECT n FROM Notice n " +
            "WHERE n.festival = :festival " +
            "AND (:categoryId IS NULL OR n.festivalCategory.category.id = :categoryId) " +
            "AND n.deletedAt IS NULL " +
            "ORDER BY n.isPinned DESC, n.createdAt DESC")
    Page<Notice> findNoticesByFilter(@Param("festival") Festival festival, @Param("categoryId") Long categoryId, Pageable pageable);
}
