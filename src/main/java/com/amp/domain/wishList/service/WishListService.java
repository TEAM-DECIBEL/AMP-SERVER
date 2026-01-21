package com.amp.domain.wishList.service;

import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.wishList.dto.request.WishListRequest;
import com.amp.domain.wishList.dto.response.*;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.wishList.dto.response.RecentWishListResponse;
import com.amp.domain.wishList.dto.response.UpdateWishListResponse;
import com.amp.domain.wishList.repository.WishListRepository;
import com.amp.global.common.dto.response.PageResponse;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListService {

    private final WishListRepository wishListRepository;
    private final FestivalRepository festivalRepository;
    private final AuthService authService;

    public Optional<RecentWishListResponse> getRecentFestival(Long userId) {
        LocalDate today = LocalDate.now();
        List<Festival> festivals = wishListRepository.findUpcomingWishlistFestivals(userId, today);

        return festivals.stream()
                .findFirst()
                .map(RecentWishListResponse::from);
    }

    @Transactional
    public UpdateWishListResponse toggleWishlist(Long festivalId, WishListRequest request) {
        User user = authService.getCurrentUser();

        if (user.getUserType() == UserType.ORGANIZER) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHENTICATED);
        }

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND));


        UserFestival userFestival = wishListRepository.findByUserAndFestival(user, festival)
                .orElseGet(() -> UserFestival.builder()
                        .user(user)
                        .festival(festival)
                        .build());

        wishListRepository.save(userFestival);
        userFestival.updateWishList(request.wishList());

        return new UpdateWishListResponse(festival.getId(), userFestival.getWishList());
    }

    @Transactional(readOnly = true)
    public PageResponse<MyUpcomingResponse> getMyWishList(Pageable pageable) {
        User user = authService.getCurrentUser();

        Page<UserFestival> userFestivals = wishListRepository.findAllByUserIdAndWishListTrue(user.getId(), pageable);
        Page<MyUpcomingResponse> responsePage = userFestivals.map(MyUpcomingResponse::of);

        return PageResponse.of(responsePage);
    }

    public PageResponse<WishListHistoryResponse> getHistoryWishList(Pageable pageable) {
        User user = authService.getCurrentUser();

        Page<UserFestival> userFestivals = wishListRepository.findAllWishListHistory(user.getId(), pageable);
        Page<WishListHistoryResponse> historyPage = userFestivals.map(WishListHistoryResponse::from);
        return PageResponse.of(historyPage);
    }
}
