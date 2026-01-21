package com.amp.domain.festival.controller.common;

import com.amp.domain.festival.dto.response.FestivalInfoResponse;
import com.amp.domain.festival.service.common.FestivalInfoService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/common/festivals")
@Tag(name = "User API")
@Tag(name = "Organizer API")
@RequiredArgsConstructor
public class FestivalInfoController {

    private final FestivalInfoService festivalInfoService;

    @Operation(summary = "공지 상단 공연 정보 조회", description = "공지 리스트 상단 표시되는 공연 정보")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_FESTIVAL_DETAIL)
    @GetMapping("/{festivalId}")
    public ResponseEntity<BaseResponse<FestivalInfoResponse>> getFestivalInfo(
            @PathVariable Long festivalId) {
        FestivalInfoResponse response = festivalInfoService.getFestivalDetail(festivalId);
        return ResponseEntity
                .status(SuccessStatus.GET_FESTIVAL_DETAIL_INFO.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.GET_FESTIVAL_DETAIL_INFO.getMsg(), response));
    }
}
