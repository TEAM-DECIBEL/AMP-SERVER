package com.amp.domain.userFestival.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.userFestival.dto.RecentFestivalResponse;
import com.amp.domain.userFestival.repository.UserFestivalRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.amp.global.common.CommonErrorCode.NO_RECENT_FESTIVAL;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFestivalService {

    private final UserFestivalRepository userFestivalRepository;

    public RecentFestivalResponse getRecentFestival(Long userId) {
        LocalDate today = LocalDate.now();

        List<Festival> upcomingWishlistFestivals = userFestivalRepository.findUpcomingWishlistFestivals(userId, today);

        if (upcomingWishlistFestivals.isEmpty()) {
            throw new CustomException(NO_RECENT_FESTIVAL);
        }

        Festival recentFestival = upcomingWishlistFestivals.get(0);
        return RecentFestivalResponse.from(recentFestival);
    }

}
