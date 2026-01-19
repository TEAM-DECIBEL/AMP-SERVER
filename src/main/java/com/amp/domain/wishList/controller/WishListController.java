package com.amp.domain.wishList.controller;

import com.amp.domain.wishList.dto.response.*;
import com.amp.domain.wishList.dto.request.WishListRequest;
import com.amp.domain.wishList.service.UserFestivalService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.common.dto.PageResponse;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/users/me/festivals")
@Tag(name = "User API")
@RequiredArgsConstructor
public class WishListController {

    private final UserFestivalService userFestivalService;

    @GetMapping("/recent")
    @Operation(summary = "관람 예정 공연 중 가장 임박한 공연 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_RECENT_WISHLIST)
    public ResponseEntity<BaseResponse<RecentWishListResponse>> getRecentFestival(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        Long userId = principal.getUserId();
        RecentWishListResponse response = userFestivalService.getRecentFestival(userId).orElse(null);

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
    public ResponseEntity<BaseResponse<UpdateWishListResponse>> toggleWishList(
            @PathVariable Long festivalId,
            @RequestBody @Valid WishListRequest request
    ) {
        UpdateWishListResponse response = userFestivalService.toggleWishlist(festivalId, request);

        SuccessStatus status = response.wishList()
                ? SuccessStatus.WISHLIST_ADDED
                : SuccessStatus.WISHLIST_REMOVED;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

    @GetMapping()
    @Operation(summary = "홈 화면 관람 예정 공연 리스트 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_WISHLISTS)
    public ResponseEntity<BaseResponse<PageResponse<MyUpcomingResponse>>> getMyWishListResponse(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MyUpcomingResponse> response = userFestivalService.getMyWishList(pageable);
        SuccessStatus status = response.festivals().isEmpty()
                ? SuccessStatus.MY_WISHLIST_IS_EMPTY
                : SuccessStatus.MY_WISHLIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

    @GetMapping("/all")
    @Operation(summary = "마이 페이지 내 관람 공연 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_WISHLISTS)
    public ResponseEntity<BaseResponse<PageResponse<WishListHistoryResponse>>> getMyAllWishListsResponse(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<WishListHistoryResponse> response = userFestivalService.getHistoryWishList(pageable);
        SuccessStatus status = response.festivals().isEmpty()
                ? SuccessStatus.MY_WISHLIST_IS_EMPTY
                : SuccessStatus.MY_WISHLIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }
}
