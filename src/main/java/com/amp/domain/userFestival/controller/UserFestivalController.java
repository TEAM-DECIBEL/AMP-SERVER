package com.amp.domain.userFestival.controller;

import com.amp.domain.userFestival.dto.response.RecentFestivalResponse;
import com.amp.domain.userFestival.dto.request.WishListRequest;
import com.amp.domain.userFestival.dto.response.MyWishListPageResponse;
import com.amp.domain.userFestival.dto.response.WishListResponse;
import com.amp.domain.userFestival.service.UserFestivalService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/festivals")
@RequiredArgsConstructor
public class UserFestivalController {

    private final UserFestivalService userFestivalService;

    @GetMapping("/recent")
    @Operation(summary = "관람 예정 공연 중 가장 임박한 공연 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_RECENT_WISHLIST)
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

    @PutMapping("/{festivalId}/wishList")
    @Operation(summary = "관람 예정 공연 등록/해제")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_ADD_WISHLIST)
    public ResponseEntity<BaseResponse<WishListResponse>> toggleWishList(
            @PathVariable Long festivalId,
            @RequestBody @Valid WishListRequest request
    ) {
        WishListResponse response = userFestivalService.toggleWishlist(festivalId, request);

        SuccessStatus status = response.wishList()
                ? SuccessStatus.WISHLIST_ADDED
                : SuccessStatus.WISHLIST_REMOVED;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

    @GetMapping("/my")
    @Operation(summary = "나의 관람 예정 공연 리스트 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_WISHLISTS)
    public ResponseEntity<BaseResponse<MyWishListPageResponse>> getMyWishListResponse(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        MyWishListPageResponse response = userFestivalService.getMyWishList(pageable);
        SuccessStatus status = response.festivals().isEmpty()
                ? SuccessStatus.MY_WISHLIST_IS_EMPTY
                : SuccessStatus.MY_WISHLIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

}
