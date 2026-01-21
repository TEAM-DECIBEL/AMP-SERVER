package com.amp.domain.notice.service.organizer;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeCreateRequest;
import com.amp.domain.notice.dto.response.Author;
import com.amp.domain.notice.dto.response.CategoryData;
import com.amp.domain.notice.dto.response.NoticeCreateResponse;
import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3ErrorCode;
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

import static com.amp.global.common.dto.TimeFormatter.formatTimeAgo;

@Service
@Slf4j
@AllArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final FestivalRepository festivalRepository;
    private final S3Service s3Service;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public NoticeCreateResponse createNotice(Long festivalId, NoticeCreateRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        if (!organizerRepository.existsByFestivalAndUser(festival, user)) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHORIZED);
        }

        FestivalCategory festivalCategory = festivalCategoryRepository
                .findByMapping(request.categoryId(), festivalId)
                .orElseThrow(() -> new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND));

        if (!festivalCategory.getFestival().getId().equals(festival.getId())) {
            throw new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND);
        }

        String imageKey = null;
        String imageUrl = null;
        Notice notice;

        try {
            if (request.image() != null && !request.image().isEmpty()) {
                imageKey = uploadImage(request.image());
                imageUrl = s3Service.getPublicUrl(imageKey);
            }

            notice = Notice.builder()
                    .title(request.title())
                    .content(request.content())
                    .imageUrl(imageUrl)
                    .isPinned(request.isPinned())
                    .user(user)
                    .festival(festival)
                    .festivalCategory(festivalCategory)
                    .build();

            noticeRepository.save(notice);

            eventPublisher.publishEvent(
                    new NoticeCreatedEvent(
                            festivalCategory.getId(),
                            festivalCategory.getCategory().getCategoryName(),
                            notice.getFestival().getTitle(),
                            notice.getId(),
                            notice.getTitle(),
                            notice.getCreatedAt()
                    )
            );

        } catch (CustomException e) {
            if (imageKey != null) {
                s3Service.delete(imageKey);
            }
            throw e;

        } catch (Exception e) {
            if (imageKey != null) {
                try {
                    s3Service.delete(imageKey);
                } catch (Exception ignored) {
                }
            }
            throw new NoticeException(NoticeErrorCode.NOTICE_CREATE_FAIL);
        }
        return new NoticeCreateResponse(notice.getId());
    }

    private String uploadImage(MultipartFile image) {
        try {
            return s3Service.upload(image, "notices");
        } catch (Exception e) {
            throw new CustomException(S3ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    private final AuthService authService;

    public NoticeDetailResponse getNoticeDetail(Long noticeId) {

        // 공지 조회 (존재 검증 포함)
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
                );

        // 로그인 여부에 따른 저장 여부 판단
        boolean isSaved = getIsSaved(notice);

        CategoryData category = new CategoryData(
                notice.getFestivalCategory().getId(),
                notice.getFestivalCategory().getCategory().getCategoryName(),
                notice.getFestivalCategory().getCategory().getCategoryCode()
        );

        Author author = new Author(
                notice.getUser().getId(),
                notice.getUser().getNickname()
        );

        return new NoticeDetailResponse(
                notice.getId(),
                notice.getFestival().getId(),
                notice.getFestival().getTitle(),
                category,
                notice.getTitle(),
                notice.getContent(),
                notice.getImageUrl(),
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

        // 로그인한 사용자만 북마크 여부 확인
        if (authService.isLoggedInUser(authentication)) {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                    new CustomException(UserErrorCode.USER_NOT_FOUND));


            isSaved = bookmarkRepository
                    .existsByNoticeAndUser(notice, user);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(UserErrorCode.USER_NOT_FOUND)
                );

        if (!notice.getUser().getId().equals(user.getId())) {
            throw new NoticeException(NoticeErrorCode.NOTICE_DELETE_FORBIDDEN);
        }

        notice.delete();
    }
}
