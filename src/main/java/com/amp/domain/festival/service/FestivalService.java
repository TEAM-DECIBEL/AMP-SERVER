package com.amp.domain.festival.service;

import com.amp.domain.category.service.FestivalCategoryService;
import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.request.FestivalUpdateRequest;
import com.amp.domain.festival.dto.request.ScheduleRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.dto.response.FestivalDetailResponse;
import com.amp.domain.festival.dto.response.FestivalUpdateResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.stage.dto.request.StageRequest;
import com.amp.domain.stage.service.StageService;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.annotation.LogExecutionTime;
import com.amp.global.common.CommonErrorCode;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3ErrorCode;
import com.amp.global.s3.S3Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;

    private final FestivalScheduleService scheduleService;
    private final StageService stageService;
    private final FestivalCategoryService categoryService;

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Transactional
    public FestivalCreateResponse createFestival(FestivalCreateRequest request) {
        User user = getCurrentUser();

        List<ScheduleRequest> schedules = parseSchedules(request.schedules());
        List<StageRequest> stages = parseStages(request.stages());
        List<Long> activeCategoryIds = parseCategoryIds(request.activeCategoryIds());

        if (schedules == null || schedules.isEmpty()) {
            throw new CustomException(FestivalErrorCode.SCHEDULES_REQUIRED);
        }

        if (request.mainImage() == null || request.mainImage().isEmpty()) {
            throw new CustomException(FestivalErrorCode.MISSING_MAIN_IMAGE);
        }

        LocalDate startDate = calculateDate(schedules, true);
        LocalDate endDate = calculateDate(schedules, false);

        String imageKey = null;
        try {
            imageKey = uploadImage(request.mainImage());
            String publicUrl = s3Service.getPublicUrl(imageKey);

            Festival festival = Festival.builder()
                    .title(request.title())
                    .location(request.location())
                    .startDate(startDate)
                    .endDate(endDate)
                    .mainImageUrl(publicUrl)
                    .build();

            festival.updateStatus();
            Festival savedFestival = festivalRepository.save(festival);

            Organizer organizer = Organizer.builder()
                    .user(user)
                    .festival(savedFestival)
                    .organizerName(user.getNickname())
                    .contactEmail(user.getEmail())
                    .build();

            organizerRepository.save(organizer);

            scheduleService.syncSchedules(savedFestival, schedules);
            if (stages != null) {
                stageService.syncStages(savedFestival, stages);
            }
            categoryService.syncCategories(savedFestival, activeCategoryIds);

            return FestivalCreateResponse.from(savedFestival);

        } catch (CustomException e) {
            if (imageKey != null) {
                s3Service.delete(imageKey);
            }

            throw e;
        } catch (Exception e) {
            if (imageKey != null) {
                try {
                    s3Service.delete(imageKey);
                } catch (Exception deleteException) {
                }
            }

            throw new CustomException(FestivalErrorCode.FESTIVAL_CREATE_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public FestivalDetailResponse getFestivalDetail(Long festivalId) {
        User user = getCurrentUser();
        Festival festival = findFestival(festivalId);

        if (!organizerRepository.existsByFestivalAndUser(festival, user)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }

        return FestivalDetailResponse.from(festival);
    }

    @Transactional
    public FestivalUpdateResponse updateFestival(Long festivalId, FestivalUpdateRequest request) {
        User user = getCurrentUser();
        Festival festival = findFestival(festivalId);

        validateOrganizer(festival, user);

        festival.updateInfo(request.title(), request.location());

        scheduleService.syncSchedules(festival, request.schedules());
        stageService.syncStages(festival, request.stages());
        categoryService.syncCategories(festival, request.activeCategoryIds());

        return FestivalUpdateResponse.from(festival);
    }

    private LocalDate calculateDate(List<ScheduleRequest> schedules, boolean isStart) {
        return schedules.stream()
                .map(ScheduleRequest::getFestivalDate)
                .min(isStart ? Comparator.naturalOrder() : Comparator.reverseOrder())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_FESTIVAL_PERIOD));
    }


    @LogExecutionTime("이미지 업로드")
    private String uploadImage(MultipartFile image) {
        try {
            return s3Service.upload(image, "festivals");
        } catch (Exception e) {
            throw new CustomException(S3ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    private List<ScheduleRequest> parseSchedules(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ScheduleRequest>>() {
            });
        } catch (Exception e) {
            throw new CustomException(FestivalErrorCode.INVALID_SCHEDULE_FORMAT);
        }
    }

    private List<StageRequest> parseStages(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<StageRequest>>() {
            });
        } catch (Exception e) {
            throw new CustomException(FestivalErrorCode.INVALID_STAGE_FORMAT);
        }
    }

    private List<Long> parseCategoryIds(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (Exception e) {
            throw new CustomException(FestivalErrorCode.INVALID_CATEGORY_FORMAT);
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    private Festival findFestival(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));
    }

    private void validateOrganizer(Festival festival, User user) {
        if (!organizerRepository.existsByFestivalAndUser(festival, user)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }
    }

}
