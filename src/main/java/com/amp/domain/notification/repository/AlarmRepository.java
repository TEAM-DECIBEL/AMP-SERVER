package com.amp.domain.notification.repository;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.notification.entity.Alarm;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Optional<Alarm> findByUserAndFestivalCategory(User user, FestivalCategory festivalCategory);

    List<Alarm> findAllByFestivalCategoryIdAndIsActiveTrue(Long categoryId);
}
