package com.amp.domain.festival.controller;

import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.request.FestivalUpdateRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.dto.response.FestivalDetailResponse;
import com.amp.domain.festival.dto.response.FestivalUpdateResponse;
import com.amp.domain.festival.service.FestivalService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @Operation(summary = "공연 생성")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_CREATE_FESTIVAL)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<FestivalCreateResponse>> createFestival
            (@ModelAttribute @Valid FestivalCreateRequest request) {
        FestivalCreateResponse response = festivalService.createFestival(request);
        return ResponseEntity
                .status(SuccessStatus.FESTIVAL_CREATE_SUCCESS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.FESTIVAL_CREATE_SUCCESS.getMsg(), response));
    }

    @Operation(summary = "공연 상세 조회 - 수정용")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_FESTIVAL_DETAIL)
    @GetMapping("/{festivalId}")
    public ResponseEntity<BaseResponse<FestivalDetailResponse>> getFestivalDetail(
            @PathVariable Long festivalId) {
        FestivalDetailResponse response = festivalService.getFestivalDetail(festivalId);
        return ResponseEntity
                .status(SuccessStatus.GET_FESTIVAL_DETAIL_INFO.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.GET_FESTIVAL_DETAIL_INFO.getMsg(), response));
    }

    @Operation(summary = "공연 수정")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UPDATE_FESTIVAL)
    @PatchMapping("/{festivalId}")
    public ResponseEntity<BaseResponse<FestivalUpdateResponse>> updateFestival(
            @PathVariable Long festivalId,
            @RequestBody @Valid FestivalUpdateRequest request) {
        FestivalUpdateResponse response = festivalService.updateFestival(festivalId, request);
        return ResponseEntity
                .status(SuccessStatus.FESTIVAL_UPDATE_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.FESTIVAL_UPDATE_SUCCESS.getMsg(), response));
    }

    @Operation(summary = "공연 삭제")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_DELETE_FESTIVAL_DETAIL)
    @DeleteMapping("/{festivalId}")
    public ResponseEntity<BaseResponse<Void>> deleteFestival(
            @PathVariable Long festivalId) {
        festivalService.deleteFestival(festivalId);
        return ResponseEntity
                .status(SuccessStatus.FESTIVAL_DELETE_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.FESTIVAL_DELETE_SUCCESS.getMsg(), null));
    }

}
