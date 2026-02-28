package com.amp.domain.notification.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.CategoryErrorCode;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.CategoryRepository;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.notification.entity.Alarm;
import com.amp.domain.notification.repository.AlarmRepository;
import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.AudienceRepository;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.fcm.exception.FCMErrorCode;
import com.amp.global.fcm.service.FCMService;
import com.amp.global.security.service.AuthService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final AudienceRepository audienceRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final AuthService authService;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void subscribe(Long festivalId, String categoryCode, String fcmToken) {

        log.info(
                "[카테고리 구독 요청 수신] - festivalId={}, categoryCode={}, fcmToken={}",
                festivalId,
                categoryCode,
                fcmToken
        );


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

        log.info("### [구독 요청 시작] 토픽ID: {}, 토큰: {}", festivalCategory.getId(), fcmToken);
        try {
            fcmService.subscribeCategory(festivalCategory.getId(), fcmToken);
            log.info("### 구글 명단 등록 요청 완료");
        } catch (Exception e) {
            log.error("### 구글 명단 등록 실패: {}", e.getMessage());
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


        log.info(
                "[FCM 토픽 구독 요청] - topicId={}, token={}",
                festivalCategory.getId(),
                fcmToken
        );

        Alarm alarm = alarmRepository.findByUserAndFestivalCategory(user, festivalCategory)
                .orElseThrow(() -> new CustomException(FCMErrorCode.NOT_SUBSCRIBED_CATEGORY));

        if (!alarm.isActive()) {
            throw new CustomException(FCMErrorCode.NOT_SUBSCRIBED_CATEGORY);
        }

        alarm.setActive(false);
        alarmRepository.save(alarm);

        log.info("### [구독 해지] 토픽ID: {}, 토큰: {}", festivalCategory.getId(), fcmToken);
        try {
            fcmService.unsubscribeCategory(festivalCategory.getId(), fcmToken);
            log.info("### 구글 명단 해지 요청 완료!");
        } catch (Exception e) {
            log.error("### 구글 명단 해지 실패: {}", e.getMessage());
        }
    }

    @Transactional
    public void registerToken(String fcmToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
        Audience audience = audienceRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        List<Alarm> activeAlarms = alarmRepository.findAllByAudienceAndIsActiveTrue(audience);

        for (Alarm alarm : activeAlarms) {
            try {
                fcmService.subscribeCategory(alarm.getFestivalCategory().getId(), fcmToken);
                log.info("[FCM 토큰 등록] audienceId={}, categoryId={}", audience.getId(), alarm.getFestivalCategory().getId());
            } catch (Exception e) {
                log.error("[FCM 토큰 등록 실패] audienceId={}, categoryId={}, error={}",
                        audience.getId(), alarm.getFestivalCategory().getId(), e.getMessage());
            }
        }
    }
}