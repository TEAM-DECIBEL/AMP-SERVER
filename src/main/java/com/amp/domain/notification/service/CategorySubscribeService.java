package com.amp.domain.notification.service;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
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
import com.google.firebase.messaging.FirebaseMessagingException;
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
    private final AlarmRepository alarmRepository; // Alarm JPA Repository
    private final UserRepository userRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final AuthService authService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void subscribe(Long categoryId, String fcmToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        FestivalCategory festivalCategory = festivalCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        // DB에 이미 구독 기록 있는지 확인
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

        // 트랜잭션 안에서 이벤트만 발행
        applicationEventPublisher.publishEvent(
                new CategorySubscribeEvent(categoryId, fcmToken, true)
        );
    }

    @Transactional
    public void unsubscribe(Long categoryId, String fcmToken) throws FirebaseMessagingException {
        // 로그인 안한 유저가 요청시 예외처리
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        FestivalCategory festivalCategory = festivalCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        Alarm alarm = alarmRepository.findByUserAndFestivalCategory(user, festivalCategory)
                .orElseThrow(() -> new CustomException(FCMErrorCode.NOT_SUBSCRIBED_CATEGORY));


        // 구독하고 있지 않은건지 확인 필요
        if (!alarm.isActive()) {
            throw new CustomException(FCMErrorCode.NOT_SUBSCRIBED_CATEGORY);
        }

        alarm.setActive(false);
        alarmRepository.save(alarm);

        applicationEventPublisher.publishEvent(
                new CategorySubscribeEvent(categoryId, fcmToken, false)
        );
    }
}
