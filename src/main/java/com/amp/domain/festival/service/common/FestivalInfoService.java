package com.amp.domain.festival.service.common;

import com.amp.domain.festival.dto.response.FestivalInfoResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.wishList.repository.WishListRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FestivalInfoService {

    private final FestivalRepository festivalRepository;
    private final AuthService authService;
    private final WishListRepository wishListRepository;

    @Transactional(readOnly = true)
    public FestivalInfoResponse getFestivalDetail(Long festivalId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        User user = authService.getCurrentUserOrNull();

        Boolean isWishlist;

        if (user == null) {
            // 비로그인 유저
            isWishlist = false;
        } else if (user.getUserType() == UserType.ORGANIZER) {
            // 주최사 유저
            isWishlist = null;
        } else {
            // 로그인 유저
            isWishlist = wishListRepository.findByUserAndFestival(user, festival)
                    .map(UserFestival::getWishList)
                    .orElse(false);
        }

        return FestivalInfoResponse.from(festival, isWishlist);
    }
}
