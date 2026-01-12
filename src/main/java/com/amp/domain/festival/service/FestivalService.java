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
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.stage.dto.request.StageRequest;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.global.annotation.LogExecutionTime;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3ErrorCode;
import com.amp.global.s3.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final StageRepository stageRepository;
    private final CategoryRepository categoryRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final S3Service s3Service;

    // 1. 유효성 검증
    // 2. 이미지 업로드
    // 3. 이미지와 함께 festival 저장
    // schedule, stage, category 도 같이 저장
    // 실패시 롤백

    @Transactional
    public FestivalCreateResponse createFestival(FestivalCreateRequest request) {

        // 카테고리 유효성 검증
        validateCategories(request.getActiveCategoryIds());

        // startDate와 endDate에 날짜 저장
        LocalDate startDate = request.getSchedules().stream()
                .map(ScheduleRequest::getFestivalDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_FESTIVAL_PERIOD));

        LocalDate endDate = request.getSchedules().stream()
                .map(ScheduleRequest::getFestivalDate)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.INVALID_FESTIVAL_PERIOD));

        // 이미지 업로드
        String imageUrl = null;
        try {
            if (request.getMainImageUrl() != null && !request.getMainImageUrl().isEmpty()) {
                imageUrl = uploadImage(request.getMainImageUrl());
                log.info("이미지 업로드 완료: url={}", imageUrl);
            }

            // 페스티벌 객체 생성
            Festival festival = Festival.builder()
                    .title(request.getTitle())
                    .location(request.getLocation())
                    .startDate(startDate)
                    .endDate(endDate)
                    .mainImageUrl(imageUrl)
                    .status(FestivalStatus.UPCOMING)
                    .build();

            Festival savedFestival = festivalRepository.save(festival);
            log.info("Festival 생성 완료: festivalId={}", savedFestival.getId());

            // 4. FestivalSchedule 생성 (dayNumber 자동 계산)
            // 사실 최소 1개 이상 값이라서 검사 안 해도 되긴함
            if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
                createSchedules(savedFestival, request);
                log.info("Schedule 생성 완료: count={}", request.getSchedules().size());
            }

            // 5. Stage 생성
            if (request.getStages() != null && !request.getStages().isEmpty()) {
                createStages(savedFestival, request.getStages());
                log.info("Stage 생성 완료: count={}", request.getStages().size());
            }

            // 6. 활성화 카테고리 Category 연결
            if (request.getActiveCategoryIds() != null && !request.getActiveCategoryIds().isEmpty()) {
                linkCategories(savedFestival, request.getActiveCategoryIds());
                log.info("Category 연결 완료: count={}", request.getActiveCategoryIds().size());
            }

            return FestivalCreateResponse.from(savedFestival);

        } catch (Exception e) {
            // 이미지 롤백
            if (imageUrl != null) {
                try {
                    s3Service.delete(imageUrl);
                    log.info("롤백: 이미지 삭제 완료: url={}", imageUrl);
                } catch (Exception deleteException) {
                    log.error("이미지 삭제 실패: {}", imageUrl, deleteException);
                }
            }

            log.error("Festival 생성 실패", e);
            throw new CustomException(FestivalErrorCode.FESTIVAL_CREATE_FAILED);
        }

    }

    // 존재하지 않는 카테고리
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
            return s3Service.upload(image, "festivals/");
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            throw new CustomException(S3ErrorCode.S3_UPLOAD_FAILED);
        }

    }

    private void createSchedules(Festival festival, FestivalCreateRequest request) {
        LocalDate startDate = festival.getStartDate();

        List<FestivalSchedule> schedules = request.getSchedules().stream()
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

        festivalScheduleRepository.saveAll(schedules);
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