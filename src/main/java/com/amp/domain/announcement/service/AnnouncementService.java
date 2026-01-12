package com.amp.domain.announcement.service;

import com.amp.domain.announcement.dto.response.AnnouncementDetailResponse;
import com.amp.domain.announcement.dto.response.Author;
import com.amp.domain.announcement.dto.response.CategoryData;
import com.amp.domain.announcement.entity.Announcement;
import com.amp.domain.announcement.exception.AnnouncementErrorCode;
import com.amp.domain.announcement.exception.AnnouncementException;
import com.amp.domain.announcement.repository.AnnouncementRepository;
import com.amp.domain.announcement.repository.UserSavedAnnouncementRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserSavedAnnouncementRepository userSavedAnnouncementRepository;
    private final UserRepository userRepository;

    public AnnouncementDetailResponse getAnnouncementDetail(Long noticeId, UserDetails userDetails) {

        // 공지 조회 (존재 검증 포함)
        Announcement announcement = announcementRepository.findById(noticeId)
                .orElseThrow(() ->
                        new AnnouncementException(AnnouncementErrorCode.INVALID_ANNOUNCEMENT)
                );

        // 로그인 여부에 따른 저장 여부 판단
        boolean isSaved = false;

        if (userDetails != null) {
            //Todo 동찬한테 물어보고 반영
            Long userId = userDetails.getAuthorities().
                    ㅁ아ㅣㅁㄴ
                    너아ㅣㅁ어ㅣㅏㅁ;

            User user = userRepository.findById(userId)
                    .orElse(null);

            isSaved = userSavedAnnouncementRepository
                    .existsByAnnouncementAndUser(announcement, user);
        }

        CategoryData category = new CategoryData(
                        announcement.getFestivalCategory().getId(),
                        announcement.getFestivalCategory().getCategory().getCategoryName(),
                        announcement.getFestivalCategory().getCategory().getCategoryCode()
                );

        Author author = new Author(
                announcement.getUser().getId(),
                announcement.getUser().getNickname()
        );

        return new AnnouncementDetailResponse(
                announcement.getId(),
                announcement.getFestival().getId(),
                announcement.getFestival().getTitle(),
                category,
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getImageUrl(),
                announcement.getIsPinned(),
                isSaved,
                author,
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }
}
