package com.amp.domain.userFestival.service;

import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.userFestival.dto.request.WishListRequest;
import com.amp.domain.userFestival.dto.response.UserFestivalListResponse;
import com.amp.domain.userFestival.dto.response.UserFestivalPageResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.userFestival.dto.response.WishListResponse;
import com.amp.domain.userFestival.repository.UserFestivalRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFestivalService {

    private final FestivalRepository festivalRepository;
    private final UserFestivalRepository userFestivalRepository;
    private final AuthService authService;

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

    @Transactional
    public WishListResponse toggleWishlist(Long festivalId, WishListRequest request) {
        User user = authService.getCurrentUser();
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));


        UserFestival userFestival = userFestivalRepository.findByUserAndFestival(user, festival)
                .orElseGet(() -> UserFestival.builder()
                        .user(user)
                        .festival(festival)
                        .build());

        userFestival.updateWishList(request.wishList());

        userFestivalRepository.save(userFestival);

        return new WishListResponse(festival.getId(), userFestival.getWishList());
    }
}
