package com.amp.domain.notice.service.organizer;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeUpdateRequest;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class NoticeUpdateService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final FestivalCategoryRepository festivalCategoryRepository;
    private final FestivalRepository festivalRepository;
    private final S3Service s3Service;

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!isLoggedInUser(authentication)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Festival festival = festivalRepository.findById(request.festivalId())
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        validateOrganizer(festival, user);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
                );

        boolean wasPinned = notice.getIsPinned();
        boolean willBePinned = request.isPinned();

        if (!wasPinned && willBePinned) {
            long pinnedCount =
                    noticeRepository.countByFestivalAndIsPinnedTrueAndDeletedAtIsNull(festival);

            if (pinnedCount >= 3) {
                throw new NoticeException(NoticeErrorCode.PINNED_NOTICE_LIMIT_EXCEEDED);
            }
        }

        if (notice.getDeletedAt() != null) {
            throw new NoticeException(NoticeErrorCode.NOTICE_ALREADY_DELETED);
        }

        if (!notice.getFestival().getId().equals(festival.getId())) {
            throw new NoticeException(NoticeErrorCode.NOTICE_UPDATE_FORBIDDEN);
        }

        FestivalCategory festivalCategory = festivalCategoryRepository
                .findById(request.categoryId())
                .orElseThrow(() ->
                        new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND)
                );

        if (!festivalCategory.getFestival().getId().equals(festival.getId())) {
            throw new NoticeException(FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND);
        }

        String imageUrl = notice.getImageUrl();
        String newImageKey = null;

        try {
            if (request.newImage() != null && !request.newImage().isEmpty()) {
                newImageKey = s3Service.upload(request.newImage(), "notices");
                imageUrl = s3Service.getPublicUrl(newImageKey);
            }

            notice.update(
                    request.title(),
                    request.content(),
                    imageUrl,
                    request.isPinned(),
                    festivalCategory
            );

        } catch (CustomException e) {
            if (newImageKey != null) {
                s3Service.delete(newImageKey);
            }
            throw e;

        } catch (Exception e) {
            if (newImageKey != null) {
                s3Service.delete(newImageKey);
            }
            throw new NoticeException(NoticeErrorCode.UPDATE_NOTICE_FAILED);
        }
    }

    private boolean isLoggedInUser(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    private void validateOrganizer(Festival festival, User user) {
        if (!festival.getOrganizer().getUser().getId().equals(user.getId())) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHORIZED);
        }
    }
}
