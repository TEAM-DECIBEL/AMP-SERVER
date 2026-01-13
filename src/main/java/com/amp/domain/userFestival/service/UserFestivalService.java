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

    private UserFestivalRepository userFestivalRepository;

    public Optional<Object> getRecentFestival(Long userId){
        LocalDate today = LocalDate.now();

        List<Festival> upcomingWishlistFestivals = userFestivalRepository.findUpcomingWishlistFestivals(userId, today);

        if (upcomingWishlistFestivals.isEmpty()) {
            return Optional.empty();
        }

        Festival recentFestival = upcomingWishlistFestivals.get(0);

        return Optional.of(RecentFestivalResponse.from(recentFestival));
    }

}
