package com.amp.domain.notice.service.organizer;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeCreateRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.amp.global.common.dto.TimeFormatter.formatTimeAgo;

@Service
@Slf4j
@AllArgsConstructor
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

        List<String> uploadedKeys = new ArrayList<>();
        try {
            for (int i = 0; i < validImages.size(); i++) {
                String key = s3Service.upload(validImages.get(i), "notices");
                uploadedKeys.add(key);
                noticeImageRepository.save(
                        NoticeImage.of(notice, s3Service.getPublicUrl(key), i)
                );
            }
        } catch (CustomException e) {
            uploadedKeys.forEach(key -> {
                try { s3Service.delete(key); } catch (Exception ignored) {}
            });
            throw e;
        } catch (Exception e) {
            uploadedKeys.forEach(key -> {
                try { s3Service.delete(key); } catch (Exception ignored) {}
            });
            throw new NoticeException(NoticeErrorCode.NOTICE_CREATE_FAIL);
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

        // 공지 조회 (존재 검증 포함)
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
                );

        // 로그인 여부에 따른 저장 여부 판단
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
                .sorted(java.util.Comparator.comparingInt(NoticeImage::getImageOrder))
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

        notice.getImages().forEach(image -> {
            try {
                s3Service.delete(s3Service.extractKey(image.getImageUrl()));
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 실패: {}", image.getImageUrl(), e);
            }
        });

        notice.delete();
    }

    private void validateOrganizer(Festival festival, Organizer organizer) {
        if (!festival.getOrganizer().getId().equals(organizer.getId())) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHORIZED);
        }
    }
}
