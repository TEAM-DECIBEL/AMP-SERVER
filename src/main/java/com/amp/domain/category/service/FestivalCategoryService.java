package com.amp.domain.category.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.CategoryErrorCode;
import com.amp.domain.category.repository.CategoryRepository;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FestivalCategoryService {
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final NoticeRepository noticeRepository;

    public void syncCategories(Festival festival, List<Long> newCategoryIds) {
        validateCategories(newCategoryIds);

        List<FestivalCategory> festivalCategories = festival.getFestivalCategories();

        for (FestivalCategory festivalCategory : festivalCategories) {
            Long categoryId = festivalCategory.getCategory().getId();
            boolean isNowSelected = newCategoryIds.contains(categoryId);

            if (festivalCategory.isActive() && !isNowSelected) {
                festivalCategory.updateStatus(false);
                noticeRepository.deleteAllByFestivalAndFestivalCategory(festival, festivalCategory);
            } else if (!festivalCategory.isActive() && isNowSelected) {
                festivalCategory.updateStatus(true);
            }
        }

        List<Long> existIds = festivalCategories.stream().map(l -> l.getCategory().getId()).toList();
        newCategoryIds.stream()
                .filter(id -> !existIds.contains(id))
                .forEach(id -> {
                    Category category = categoryRepository.findById(id).orElseThrow();
                    festivalCategoryRepository.save(new FestivalCategory(festival, category));
                });
    }

    private void validateCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
    }
}
