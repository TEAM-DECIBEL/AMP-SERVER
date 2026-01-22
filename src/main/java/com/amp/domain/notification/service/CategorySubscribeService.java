package com.amp.domain.notification.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.CategoryErrorCode;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.CategoryRepository;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.notification.entity.Alarm;
import com.amp.domain.notification.entity.CategorySubscribeEvent;
import com.amp.domain.notification.repository.AlarmRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.fcm.exception.FCMErrorCode;
import com.amp.global.fcm.service.FCMService;
import com.amp.global.security.service.AuthService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorySubscribeService {

    private final FCMService fcmService;
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final AuthService authService;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void subscribe(Long festivalId, String categoryCode, String fcmToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        FestivalCategory festivalCategory = festivalCategoryRepository
                .findByMapping(festivalId, category.getId())
                .orElseThrow(() -> new CustomException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        Alarm alarm = alarmRepository.findByUserAndFestivalCategory(user, festivalCategory)
                .orElse(null);

        if (alarm != null && alarm.isActive()) {
            throw new CustomException(FCMErrorCode.ALREADY_SUBSCRIBED);
        }

        if (alarm == null) {
            alarm = new Alarm(user, festivalCategory);
        } else {
            alarm.setActive(true);
        }

        alarmRepository.save(alarm);

        log.info("### [긴급] 직접 구독 요청 시작. 토픽ID: {}, 토큰: {}", festivalCategory.getId(), fcmToken);
        try {
            fcmService.subscribeCategory(festivalCategory.getId(), fcmToken);
            log.info("### [긴급] 구글 명단 등록 요청 완료!");
        } catch (Exception e) {
            log.error("### [긴급] 구글 명단 등록 실패: {}", e.getMessage());
            // 트랜잭션 롤백을 원하시면 여기서 throw e;를 하시면 됩니다.
        }
    }

    @Transactional
    public void unsubscribe(Long festivalId, String categoryCode, String fcmToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
        FestivalCategory festivalCategory = festivalCategoryRepository
                .findByMapping(festivalId, category.getId())
                .orElseThrow(() -> new CustomException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        Alarm alarm = alarmRepository.findByUserAndFestivalCategory(user, festivalCategory)
                .orElseThrow(() -> new CustomException(FCMErrorCode.NOT_SUBSCRIBED_CATEGORY));

        if (!alarm.isActive()) {
            throw new CustomException(FCMErrorCode.NOT_SUBSCRIBED_CATEGORY);
        }

        alarm.setActive(false);
        alarmRepository.save(alarm);

        log.info("### [긴급] 직접 구독 해지 시작. 토픽ID: {}, 토큰: {}", festivalCategory.getId(), fcmToken);
        try {
            fcmService.unsubscribeCategory(festivalCategory.getId(), fcmToken);
            log.info("### [긴급] 구글 명단 해지 요청 완료!");
        } catch (Exception e) {
            log.error("### [긴급] 구글 명단 해지 실패: {}", e.getMessage());
        }
    }
}