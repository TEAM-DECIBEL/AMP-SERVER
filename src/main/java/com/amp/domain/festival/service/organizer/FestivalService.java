package com.amp.domain.festival.service.organizer;

import com.amp.domain.category.exception.CategoryErrorCode;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.category.service.FestivalCategoryService;
import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.request.FestivalUpdateRequest;
import com.amp.domain.festival.dto.request.ScheduleRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.dto.response.FestivalDetailResponse;
import com.amp.domain.festival.dto.response.FestivalUpdateResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.festival.scheduler.FestivalScheduleService;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.stage.dto.request.StageRequest;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.stage.service.StageService;
import com.amp.domain.user.entity.User;
import com.amp.global.annotation.LogExecutionTime;
import com.amp.global.common.CommonErrorCode;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3ErrorCode;
import com.amp.global.s3.S3Service;
import com.amp.global.security.service.AuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final OrganizerRepository organizerRepository;
    private final StageRepository stageRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;

    private final FestivalScheduleService scheduleService;
    private final StageService stageService;
    private final FestivalCategoryService categoryService;
    private final AuthService authService;

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Transactional
    public FestivalCreateResponse createFestival(FestivalCreateRequest request) {
        User user = authService.getCurrentUser();

        List<ScheduleRequest> schedules = parseJson(request.schedules(), new TypeReference<List<ScheduleRequest>>() {
        }, FestivalErrorCode.INVALID_SCHEDULE_FORMAT);

        List<StageRequest> stages = parseJson(request.stages(), new TypeReference<List<StageRequest>>() {
        }, FestivalErrorCode.INVALID_STAGE_FORMAT);

        List<Long> activeCategoryIds = parseJson(request.activeCategoryIds(), new TypeReference<List<Long>>() {
        }, FestivalErrorCode.INVALID_CATEGORY_FORMAT);

        if (schedules == null || schedules.isEmpty()) {
            throw new CustomException(FestivalErrorCode.SCHEDULES_REQUIRED);
        }
        if (activeCategoryIds == null || activeCategoryIds.isEmpty()) {
            throw new CustomException(CategoryErrorCode.CATEGORY_REQUIRED);
        }

        LocalDate startDate = calculateDate(schedules, ScheduleRequest::getFestivalDate, true);
        LocalDate endDate = calculateDate(schedules, ScheduleRequest::getFestivalDate, false);
        LocalTime startTime = calculateTime(schedules, ScheduleRequest::getFestivalTime);

        if (request.mainImage() == null || request.mainImage().isEmpty()) {
            throw new CustomException(FestivalErrorCode.MISSING_MAIN_IMAGE);
        }

        String imageKey = null;
        try {
            imageKey = uploadImage(request.mainImage());
            String publicUrl = s3Service.getPublicUrl(imageKey);

            Festival festival = Festival.builder()
                    .title(request.title())
                    .location(request.location())
                    .startDate(startDate)
                    .endDate(endDate)
                    .startTime(startTime)
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
            stageService.syncStages(savedFestival, stages);
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
        User user = authService.getCurrentUser();
        Festival festival = findFestival(festivalId);

        validateOrganizer(festival, user);

        return FestivalDetailResponse.from(festival);
    }

    @Transactional
    public FestivalUpdateResponse updateFestival(Long festivalId, FestivalUpdateRequest request) {
        User user = authService.getCurrentUser();
        Festival festival = findFestival(festivalId);

        validateOrganizer(festival, user);

        festival.updateInfo(request.title(), request.location());

        scheduleService.syncSchedules(festival, request.schedules());
        stageService.syncStages(festival, request.stages());
        categoryService.syncCategories(festival, request.activeCategoryIds());

        LocalDate startDate = calculateDate(festival.getSchedules(), FestivalSchedule::getFestivalDate, true);
        LocalDate endDate = calculateDate(festival.getSchedules(), FestivalSchedule::getFestivalDate, false);
        LocalTime startTime = calculateTime(festival.getSchedules(), FestivalSchedule::getFestivalTime);

        festival.updateDates(startDate, endDate);
        festival.updateStartTime(startTime);
        festival.updateStatus();

        return FestivalUpdateResponse.from(festival);
    }

    @Transactional
    public void deleteFestival(Long festivalId) {
        User user = authService.getCurrentUser();
        Festival festival = findFestival(festivalId);
        validateOrganizer(festival, user);

        festivalScheduleRepository.softDeleteByFestivalId(festivalId);
        stageRepository.softDeleteByFestivalId(festivalId);
        organizerRepository.softDeleteByFestivalId(festivalId);
        festivalCategoryRepository.softDeleteByFestivalId(festivalId);

        festivalRepository.softDeleteById(festivalId);

    }

    private <T> LocalDate calculateDate(List<T> schedules, Function<T, LocalDate> dateExtractor, boolean isStart) {
        return schedules.stream()
                .map(dateExtractor)
                .reduce(isStart ? BinaryOperator.minBy(Comparator.naturalOrder()) : BinaryOperator.maxBy(Comparator.naturalOrder()))
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_FESTIVAL_PERIOD));
    }

    private <T> LocalTime calculateTime(List<T> schedules, Function<T, LocalTime> timeExtractor) {
        return schedules.stream()
                .map(timeExtractor)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_SCHEDULE_FORMAT));
    }

    @LogExecutionTime("이미지 업로드")
    private String uploadImage(MultipartFile image) {
        try {
            return s3Service.upload(image, "festivals");
        } catch (Exception e) {
            throw new CustomException(S3ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    private <T> T parseJson(String json, TypeReference<T> typeReference, FestivalErrorCode errorCode) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new CustomException(errorCode);
        }
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
