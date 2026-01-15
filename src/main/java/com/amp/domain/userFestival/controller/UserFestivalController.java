package com.amp.domain.userFestival.controller;

import com.amp.domain.userFestival.dto.response.RecentFestivalResponse;
import com.amp.domain.userFestival.dto.request.WishListRequest;
import com.amp.domain.userFestival.dto.response.UserFestivalPageResponse;
import com.amp.domain.userFestival.dto.response.WishListResponse;
import com.amp.domain.userFestival.service.UserFestivalService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/festivals")
@RequiredArgsConstructor
public class UserFestivalController {

    private final UserFestivalService userFestivalService;

    @GetMapping("/recent")
    public ResponseEntity<BaseResponse<RecentFestivalResponse>> getRecentFestival(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        RecentFestivalResponse response = userFestivalService.getRecentFestival(userId).orElse(null);

        if (response == null) {
            return ResponseEntity
                    .status(SuccessStatus.USER_FESTIVAL_RECENT_NOT_FOUND.getHttpStatus())
                    .body(BaseResponse.of(SuccessStatus.USER_FESTIVAL_RECENT_NOT_FOUND, null));
        }

        return ResponseEntity
                .status(SuccessStatus.USER_FESTIVAL_RECENT_FOUND.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_FESTIVAL_RECENT_FOUND, response));
    }

    
    @GetMapping
    public ResponseEntity<BaseResponse<UserFestivalPageResponse>> getAllFestivalLists(
            @PageableDefault(size = 20) Pageable pageable) {

        UserFestivalPageResponse response = userFestivalService.getAllFestivalLists(pageable);

        SuccessStatus status = response.isEmpty()
                ? SuccessStatus.FESTIVAL_LIST_EMPTY
                : SuccessStatus.FESTIVAL_LIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

    @PutMapping("{festivalId}/wishList")
    public ResponseEntity<BaseResponse<WishListResponse>> toggleWishList(
            @PathVariable Long festivalId,
            @RequestBody WishListRequest request
    ) {
        WishListResponse response = userFestivalService.toggleWishlist(festivalId, request);

        SuccessStatus status = response.wishlist()
                ? SuccessStatus.WISHLIST_ADDED
                : SuccessStatus.WISHLIST_REMOVED;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }
}
