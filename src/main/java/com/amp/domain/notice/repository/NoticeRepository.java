package com.amp.domain.notice.repository;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    void deleteAllByFestivalAndFestivalCategory(Festival festival, FestivalCategory festivalCategory);}
