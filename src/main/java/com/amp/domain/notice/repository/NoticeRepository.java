package com.amp.domain.notice.repository;

import com.amp.domain.category.entity.Category;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    void deleteAllByFestivalAndCategory(Festival festival, Category category);
}
