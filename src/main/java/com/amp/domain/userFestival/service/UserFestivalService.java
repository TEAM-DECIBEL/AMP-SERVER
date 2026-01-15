package com.amp.domain.userFestival.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.userFestival.dto.RecentFestivalResponse;
import com.amp.domain.userFestival.repository.UserFestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFestivalService {

    private final UserFestivalRepository userFestivalRepository;
    private final FestivalRepository festivalRepository;
    private final AuthService authService;

    public Optional<RecentFestivalResponse> getRecentFestival(Long userId) {
        LocalDate today = LocalDate.now();
        List<Festival> festivals = userFestivalRepository.findUpcomingWishlistFestivals(userId, today);

        return festivals.stream()
                .findFirst()
                .map(RecentFestivalResponse::from);
    }



    @Transactional(readOnly = true)
    public UserFestivalPageResponse getAllFestivalLists(Pageable pageable) {
        User user = authService.getCurrentUserOrNull();
        Page<Festival> festivalPage = festivalRepository.findAllByDeletedAtIsNull(pageable);

        if (festivalPage.isEmpty()) {
            return UserFestivalPageResponse.of(Page.empty(pageable));
        }

        Set<Long> wishlistIds = (user != null)
                ? userFestivalRepository.findAllFestivalIdsByUserId(user.getId())
                : Collections.emptySet();

        Page<UserFestivalListResponse> festivalList = festivalPage.map(f ->
                UserFestivalListResponse.from(f, wishlistIds.contains(f.getId()))
        );

        return UserFestivalPageResponse.of(festivalList);
    }
}
