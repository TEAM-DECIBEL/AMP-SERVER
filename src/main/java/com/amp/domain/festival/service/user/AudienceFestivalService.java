package com.amp.domain.festival.service.user;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.festival.dto.response.AudienceFestivalSummaryResponse;
import com.amp.domain.wishList.repository.WishListRepository;
import com.amp.global.common.dto.PageResponse;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudienceFestivalService {

    private final WishListRepository userFestivalRepository;
    private final FestivalRepository festivalRepository;
    private final AuthService authService;


    @Transactional(readOnly = true)
    public PageResponse<AudienceFestivalSummaryResponse> getAllFestivals(Pageable pageable) {
        User user = authService.getCurrentUserOrNull();
        Page<Festival> festivalPage = festivalRepository.findActiveFestivals(pageable);

        Set<Long> wishlistIds = (user != null)
                ? userFestivalRepository.findAllFestivalIdsByUserId(user.getId())
                : Collections.emptySet();

        Page<AudienceFestivalSummaryResponse> festivalList = festivalPage.map(f ->
                AudienceFestivalSummaryResponse.from(f, wishlistIds.contains(f.getId()))
        );

        return PageResponse.of(festivalList);
    }

}
