package com.amp.domain.notice.service.organizer;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeCreateRequest;
import com.amp.domain.notice.dto.request.NoticeUpdateRequest;
import com.amp.domain.notice.dto.response.Author;
import com.amp.global.common.dto.CategoryData;
import com.amp.domain.notice.dto.response.NoticeCreateResponse;
import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.NoticeImage;
import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.notice.repository.NoticeImageRepository;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.AudienceRepository;
import com.amp.domain.user.repository.OrganizerRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3Service;
import com.amp.global.security.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.amp.global.common.dto.TimeFormatter.formatTimeAgo;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final OrganizerRepository organizerRepository;
    private final AudienceRepository audienceRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final FestivalRepository festivalRepository;
    private final S3Service s3Service;

    private final ApplicationEventPublisher eventPublisher;
    private final AuthService authService;

    @Transactional
    public NoticeCreateResponse createNotice(Long festivalId, NoticeCreateRequest request,
                                             List<MultipartFile> images) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        Organizer organizer = organizerRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        validateOrganizer(festival, organizer);

        FestivalCategory festivalCategory = festivalCategoryRepository
                .findByMapping(festivalId, request.categoryId())
                .orElseThrow(() -> new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        if (request.isPinned()) {
            long pinnedCount =
                    noticeRepository.countByFestivalAndIsPinnedTrueAndDeletedAtIsNull(festival);

            if (pinnedCount >= 3) {
                throw new NoticeException(NoticeErrorCode.PINNED_NOTICE_LIMIT_EXCEEDED);
            }
        }

        if (!festivalCategory.getFestival().getId().equals(festival.getId())) {
            throw new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND);
        }

        List<MultipartFile> validImages = (images != null)
                ? images.stream().filter(f -> f != null && !f.isEmpty()).toList()
                : List.of();

        if (validImages.size() > 20) {
            throw new NoticeException(NoticeErrorCode.NOTICE_IMAGE_LIMIT_EXCEEDED);
        }

        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .isPinned(request.isPinned())
                .organizer(organizer)
                .festival(festival)
                .festivalCategory(festivalCategory)
                .build();

        noticeRepository.save(notice);

        String[] keys = uploadImagesInParallel(validImages, NoticeErrorCode.NOTICE_CREATE_FAIL);

        for (int i = 0; i < keys.length; i++) {
            noticeImageRepository.save(NoticeImage.of(notice, s3Service.getPublicUrl(keys[i]), i));
        }

        eventPublisher.publishEvent(
                new NoticeCreatedEvent(
                        festivalCategory.getId(),
                        festivalCategory.getCategory().getCategoryName(),
                        notice.getFestival().getTitle(),
                        notice,
                        notice.getTitle(),
                        notice.getCreatedAt()
                )
        );

        return new NoticeCreateResponse(notice.getId());
    }

    public NoticeDetailResponse getNoticeDetail(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
                );

        boolean isSaved = getIsSaved(notice);

        CategoryData category = new CategoryData(
                notice.getFestivalCategory().getCategory().getId(),
                notice.getFestivalCategory().getCategory().getCategoryName(),
                notice.getFestivalCategory().getCategory().getCategoryCode()
        );

        Author author = new Author(
                notice.getOrganizer().getId(),
                notice.getOrganizer().getOrganizerName()
        );

        List<String> imageUrls = notice.getImages().stream()
                .sorted(Comparator.comparingInt(NoticeImage::getImageOrder))
                .map(NoticeImage::getImageUrl)
                .toList();

        return new NoticeDetailResponse(
                notice.getId(),
                notice.getFestival().getId(),
                notice.getFestival().getTitle(),
                category,
                notice.getTitle(),
                notice.getContent(),
                imageUrls,
                notice.getIsPinned(),
                isSaved,
                author,
                formatTimeAgo(notice.getCreatedAt())
        );
    }

    private boolean getIsSaved(Notice notice) {
        boolean isSaved = false;

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 로그인한 사용자만 북마크 여부 확인 (Audience만 북마크 가능)
        if (authService.isLoggedInUser(authentication)) {
            String userEmail = authentication.getName();
            Audience audience = audienceRepository.findByEmail(userEmail).orElse(null);
            if (audience != null) {
                isSaved = bookmarkRepository.existsByNoticeAndAudience(notice, audience);
            }
        }
        return isSaved;
    }

    @Transactional
    public void deleteNotice(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));

        if (notice.getDeletedAt() != null) {
            throw new NoticeException(NoticeErrorCode.NOTICE_ALREADY_DELETED);
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        String email = authentication.getName();
        Organizer organizer = organizerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(UserErrorCode.USER_NOT_FOUND)
                );

        if (!notice.getOrganizer().getId().equals(organizer.getId())) {
            throw new NoticeException(NoticeErrorCode.NOTICE_DELETE_FORBIDDEN);
        }

        List<String> imageKeys = notice.getImages().stream()
                .map(img -> s3Service.extractKey(img.getImageUrl()))
                .toList();

        notice.delete();

        deleteS3AfterCommit(imageKeys);
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request,
                             List<MultipartFile> newImages) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        Organizer organizer = organizerRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Festival festival = festivalRepository.findById(request.festivalId())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        validateOrganizer(festival, organizer);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));

        if (notice.getDeletedAt() != null) {
            throw new NoticeException(NoticeErrorCode.NOTICE_ALREADY_DELETED);
        }

        if (!notice.getFestival().getId().equals(festival.getId())) {
            throw new NoticeException(NoticeErrorCode.NOTICE_UPDATE_FORBIDDEN);
        }

        boolean wasPinned = notice.getIsPinned();
        boolean willBePinned = request.isPinned();

        if (!wasPinned && willBePinned) {
            long pinnedCount =
                    noticeRepository.countByFestivalAndIsPinnedTrueAndDeletedAtIsNull(festival);

            if (pinnedCount >= 3) {
                throw new NoticeException(NoticeErrorCode.PINNED_NOTICE_LIMIT_EXCEEDED);
            }
        }

        FestivalCategory festivalCategory = festivalCategoryRepository
                .findByMapping(request.festivalId(), request.categoryId())
                .orElseThrow(() -> new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        if (!festivalCategory.getFestival().getId().equals(festival.getId())) {
            throw new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND);
        }

        notice.update(request.title(), request.content(), request.isPinned(), festivalCategory);
        syncImages(notice, request.keepImageUrls(), newImages);
    }

    private void syncImages(Notice notice, List<String> keepImageUrls,
                            List<MultipartFile> newImages) {

        List<String> keepUrls = (keepImageUrls != null) ? keepImageUrls : List.of();

        List<MultipartFile> validNewImages = (newImages != null)
                ? newImages.stream().filter(f -> f != null && !f.isEmpty()).toList()
                : List.of();

        if (keepUrls.size() + validNewImages.size() > 20) {
            throw new NoticeException(NoticeErrorCode.NOTICE_IMAGE_LIMIT_EXCEEDED);
        }

        List<NoticeImage> currentImages = notice.getImages();
        Map<String, NoticeImage> imageMap = currentImages.stream()
                .collect(Collectors.toMap(NoticeImage::getImageUrl, img -> img));

        List<NoticeImage> imagesToDelete = currentImages.stream()
                .filter(img -> !keepUrls.contains(img.getImageUrl()))
                .toList();

        List<String> keysToDelete = imagesToDelete.stream()
                .map(img -> s3Service.extractKey(img.getImageUrl()))
                .toList();

        currentImages.removeAll(imagesToDelete);
        deleteS3AfterCommit(keysToDelete);

        List<NoticeImage> keptImages = keepUrls.stream()
                .filter(imageMap::containsKey)
                .map(imageMap::get)
                .toList();

        for (int i = 0; i < keptImages.size(); i++) {
            keptImages.get(i).updateOrder(i);
        }

        int startOrder = keptImages.size();

        if (validNewImages.isEmpty()) {
            return;
        }

        String[] keys = uploadImagesInParallel(validNewImages, NoticeErrorCode.UPDATE_NOTICE_FAILED);

        for (int i = 0; i < keys.length; i++) {
            noticeImageRepository.save(NoticeImage.of(notice, s3Service.getPublicUrl(keys[i]), startOrder + i));
        }
    }

    private String[] uploadImagesInParallel(List<MultipartFile> images, NoticeErrorCode failCode) {
        String[] keys = new String[images.size()];

        List<CompletableFuture<Void>> futures = IntStream.range(0, images.size())
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> keys[i] = s3Service.upload(images.get(i), "notices")
                ))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null)
                .join();

        boolean anyFailed = futures.stream().anyMatch(CompletableFuture::isCompletedExceptionally);
        if (anyFailed) {
            Arrays.stream(keys).filter(Objects::nonNull).forEach(key -> {
                try { s3Service.delete(key); } catch (Exception ignored) {}
            });
            throw new NoticeException(failCode);
        }

        return keys;
    }

    private void deleteS3AfterCommit(List<String> keys) {
        if (keys.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keys.forEach(key -> {
                    try {
                        s3Service.delete(key);
                    } catch (Exception e) {
                        log.warn("S3 이미지 삭제 실패: {}", key, e);
                    }
                });
            }
        });
    }

    private void validateOrganizer(Festival festival, Organizer organizer) {
        if (!festival.getOrganizer().getId().equals(organizer.getId())) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHORIZED);
        }
    }
}
