package com.amp.domain.festival.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.CategoryErrorCode;
import com.amp.domain.category.repository.CategoryRepository;
import com.amp.domain.category.repository.FestivalCategoryRepository;
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
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.stage.dto.request.StageRequest;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.repository.StageRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final StageRepository stageRepository;
    private final CategoryRepository categoryRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final NoticeRepository noticeRepository;
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

        if (request.mainImage() == null || request.mainImage().isEmpty()) {
            throw new CustomException(FestivalErrorCode.MISSING_MAIN_IMAGE);
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

            createSchedules(savedFestival, schedules);

            if (stages != null && !stages.isEmpty()) {
                createStages(savedFestival, stages);
            }

            if (activeCategoryIds != null && !activeCategoryIds.isEmpty()) {
                linkCategories(savedFestival, activeCategoryIds);
            }

            return FestivalCreateResponse.from(savedFestival);

        } catch (CustomException e) {
            if (imageKey != null) {
                s3Service.delete(imageKey);
            }

            throw e;
        } catch (
                Exception e) {
            if (imageKey != null) {
                try {
                    s3Service.delete(imageKey);
                } catch (Exception deleteException) {
                }
            }

            throw new CustomException(FestivalErrorCode.FESTIVAL_CREATE_FAILED);
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

    private void createSchedules(Festival festival, List<ScheduleRequest> schedules) {
        List<FestivalSchedule> festivalSchedules = schedules.stream()
                .map(req -> {

                    return FestivalSchedule.builder()
                            .festival(festival)
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

    public FestivalDetailResponse getFestivalDetail(Long festivalId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new CustomException(UserErrorCode.USER_NOT_FOUND));

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        if (!organizerRepository.existsByFestivalAndUser(festival, user)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }

        return FestivalDetailResponse.from(festival);
    }

    public FestivalUpdateResponse updateFestival(Long festivalId, FestivalUpdateRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new CustomException(UserErrorCode.USER_NOT_FOUND));

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        if (!organizerRepository.existsByFestivalAndUser(festival, user)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }

        festival.updateInfo(festival.getTitle(), festival.getLocation());

        List<ScheduleRequest> scheduleRequests = parseSchedules(request.schedules());
        updateSchedules(festival, scheduleRequests);

        List<StageRequest> stageRequests = parseStages(request.stages());
        updateStages(festival, stageRequests);

        updateCategoriesAndNotices(festival, request.activeCategoryIds());

        return FestivalUpdateResponse.from(festival);
    }

    private void updateSchedules(Festival festival, List<ScheduleRequest> requests) {
        List<FestivalSchedule> existSchedules = festival.getSchedules();

        Map<Long, FestivalSchedule> scheduleMap = existSchedules.stream()
                .collect(Collectors.toMap(FestivalSchedule::getId, Function.identity()));

        Set<Long> requestIdSet = requests.stream()
                .map(ScheduleRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existSchedules.removeIf(s -> !requestIdSet.contains(s.getId()));

        for (ScheduleRequest request : requests) {
            if (request.getId() != null && scheduleMap.containsKey(request.getId())) {
                FestivalSchedule schedule = scheduleMap.get(request.getId());
                schedule.update(request.getFestivalDate(), request.getFestivalTime());
            } else {
                existSchedules.add(FestivalSchedule.builder()
                        .festival(festival)
                        .festivalDate(request.getFestivalDate())
                        .festivalTime(request.getFestivalTime())
                        .build());
            }
        }
    }

    private void updateStages(Festival festival, List<StageRequest> requests) {
        List<Stage> existStages = festival.getStages();

        Map<Long, Stage> stageMap = existStages.stream()
                .collect(Collectors.toMap(Stage::getId, Function.identity()));

        Set<Long> requestIds = requests.stream()
                .map(StageRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existStages.removeIf(stage -> !requestIds.contains(stage.getId()));

        for (StageRequest request : requests) {
            if (request.getId() != null && stageMap.containsKey(request.getId())) {
                stageMap.get(request.getId()).update(request.getTitle(), request.getLocation());
            } else {
                existStages.add(Stage.builder()
                        .festival(festival)
                        .title(request.getTitle())
                        .location(request.getLocation())
                        .build());
            }
        }
    }

    private void updateCategoriesAndNotices(Festival festival, List<Long> newCategoryIds) {
        List<FestivalCategory> currentCategories = festival.getFestivalCategories();

        for (FestivalCategory category : currentCategories) {
            Long categoryId = category.getCategory().getId();
            boolean isNowSelected = newCategoryIds.contains(categoryId);

            if (category.isActive() && !isNowSelected) {
                category.updateStatus(false);
                noticeRepository.deleteAllByFestivalAndCategory(festival, category.getCategory());
            } else if (!category.isActive() && isNowSelected) {
                category.updateStatus(true);
            }
        }

        List<Long> existIds = currentCategories.stream()
                .map(link -> link.getCategory().getId())
                .toList();

        newCategoryIds.stream()
                .filter(id -> !existIds.contains(id))
                .forEach(id -> {
                    Category category = categoryRepository.findById(id)
                            .orElseThrow(() -> new CustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));

                    currentCategories.add(FestivalCategory.builder()
                            .festival(festival)
                            .category(category)
                            .build());
                });
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


}
