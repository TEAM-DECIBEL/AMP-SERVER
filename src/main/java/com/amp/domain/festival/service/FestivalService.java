package com.amp.domain.festival.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.CategoryErrorCode;
import com.amp.domain.category.repository.CategoryRepository;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.request.ScheduleRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.stage.dto.request.StageRequest;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.annotation.LogExecutionTime;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3ErrorCode;
import com.amp.global.s3.S3Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final StageRepository stageRepository;
    private final CategoryRepository categoryRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;

    private final S3Service s3Service;

    private final ObjectMapper objectMapper;

    @Transactional
    public FestivalCreateResponse createFestival(FestivalCreateRequest request) {

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new CustomException(UserErrorCode.USER_NOT_FOUND));

        List<ScheduleRequest> schedules = parseSchedules(request.schedules());
        List<StageRequest> stages = parseStages(request.stages());
        List<Long> activeCategoryIds = parseCategoryIds(request.activeCategoryIds());

        if (schedules == null || schedules.isEmpty()) {
            throw new CustomException(FestivalErrorCode.SCHEDULES_REQUIRED);
        }

        validateCategories(activeCategoryIds);

        LocalDate startDate = schedules.stream()
                .map(ScheduleRequest::getFestivalDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_FESTIVAL_PERIOD));

        LocalDate endDate = schedules.stream()
                .map(ScheduleRequest::getFestivalDate)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_FESTIVAL_PERIOD));

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

            createSchedules(savedFestival, schedules, startDate);

            if (stages != null && !stages.isEmpty()) {
                createStages(savedFestival, stages);
            }

            if (activeCategoryIds != null && !activeCategoryIds.isEmpty()) {
                linkCategories(savedFestival, activeCategoryIds);
            }

            return FestivalCreateResponse.from(savedFestival);

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

    private void validateCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    @LogExecutionTime("이미지 업로드")
    private String uploadImage(MultipartFile image) {
        try {
            return s3Service.upload(image, "festivals");
        } catch (Exception e) {
            throw new CustomException(S3ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    private void createSchedules(Festival festival, List<ScheduleRequest> schedules, LocalDate startDate) {
        List<FestivalSchedule> festivalSchedules = schedules.stream()
                .map(req -> {
                    int dayNumber = (int) ChronoUnit.DAYS.between(startDate, req.getFestivalDate()) + 1;

                    return FestivalSchedule.builder()
                            .festival(festival)
                            .dayNumber(dayNumber)
                            .festivalDate(req.getFestivalDate())
                            .festivalTime(req.getFestivalTime())
                            .build();
                })
                .collect(Collectors.toList());

        festivalScheduleRepository.saveAll(festivalSchedules);
    }

    private void createStages(Festival festival, List<StageRequest> stageRequests) {
        List<Stage> stages = stageRequests.stream()
                .map(request -> Stage.builder()
                        .festival(festival)
                        .title(request.getTitle())
                        .location(request.getLocation())
                        .build())
                .collect(Collectors.toList());

        stageRepository.saveAll(stages);
    }

    private void linkCategories(Festival festival, List<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        List<FestivalCategory> festivalCategories = categories.stream()
                .map(category -> new FestivalCategory(festival, category))
                .collect(Collectors.toList());

        festivalCategoryRepository.saveAll(festivalCategories);
    }
}
