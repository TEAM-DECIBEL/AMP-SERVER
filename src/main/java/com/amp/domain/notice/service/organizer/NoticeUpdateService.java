package com.amp.domain.notice.service.organizer;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeUpdateRequest;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.NoticeImage;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeImageRepository;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.OrganizerRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class NoticeUpdateService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final OrganizerRepository organizerRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final FestivalRepository festivalRepository;
    private final S3Service s3Service;

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request,
                             List<MultipartFile> newImages) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!isLoggedInUser(authentication)) {
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

        List<String> keepUrls;
        if (keepImageUrls != null) {
            keepUrls = keepImageUrls;
        } else {
            keepUrls = List.of();
        }

        List<MultipartFile> validNewImages;
        if (newImages != null) {
            validNewImages = newImages.stream().filter(f -> f != null && !f.isEmpty()).toList();
        } else {
            validNewImages = List.of();
        }

        if (keepUrls.size() + validNewImages.size() > 20) {
            throw new NoticeException(NoticeErrorCode.NOTICE_IMAGE_LIMIT_EXCEEDED);
        }

        List<NoticeImage> currentImages = notice.getImages();
        Map<String, NoticeImage> imageMap = currentImages.stream()
                .collect(Collectors.toMap(NoticeImage::getImageUrl, img -> img));

        List<NoticeImage> imagesToDelete = currentImages.stream()
                .filter(img -> !keepUrls.contains(img.getImageUrl()))
                .toList();

        imagesToDelete.forEach(img -> {
            try {
                s3Service.delete(s3Service.extractKey(img.getImageUrl()));
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 실패: {}", img.getImageUrl(), e);
            }
        });
        currentImages.removeAll(imagesToDelete);

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

        String[] keys = new String[validNewImages.size()];

        List<CompletableFuture<Void>> futures = IntStream.range(0, validNewImages.size())
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> keys[i] = s3Service.upload(validNewImages.get(i), "notices")
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
            throw new NoticeException(NoticeErrorCode.UPDATE_NOTICE_FAILED);
        }

        for (int i = 0; i < keys.length; i++) {
            noticeImageRepository.save(NoticeImage.of(notice, s3Service.getPublicUrl(keys[i]), startOrder + i));
        }
    }

    private boolean isLoggedInUser(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    private void validateOrganizer(Festival festival, Organizer organizer) {
        if (!festival.getOrganizer().getId().equals(organizer.getId())) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHORIZED);
        }
    }
}
