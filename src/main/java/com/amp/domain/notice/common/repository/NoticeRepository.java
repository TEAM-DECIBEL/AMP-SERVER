package com.amp.domain.notice.common.repository;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.notice.common.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<Notice> findById(Long id);

    void deleteAllByFestivalAndFestivalCategory(Festival festival, FestivalCategory festivalCategory);

    Page<Notice> findAllByFestival(Festival festival, Pageable pageable);
}
