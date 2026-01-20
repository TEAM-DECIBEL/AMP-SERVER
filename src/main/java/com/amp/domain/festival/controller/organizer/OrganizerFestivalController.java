package com.amp.domain.festival.controller.organizer;

import com.amp.domain.festival.dto.response.OrganizerActiveFestivalPageResponse;
import com.amp.domain.festival.dto.response.OrganizerFestivalListResponse;
import com.amp.domain.festival.service.organizer.OrganizerFestivalService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.common.dto.PageResponse;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/organizer/me/festivals")
@Tag(name = "Organizer API")
@RequiredArgsConstructor
public class OrganizerFestivalController {

    private final OrganizerFestivalService organizerFestivalService;

    @Operation(summary = "진행한 모든 공연 조회", description = "주최사가 등록한 모든 공연 중 삭제된 것을 제외하고 전부 조회")
    @ApiErrorCodes(SwaggerResponseDescription.NO_AUTHORIZATION)
    @GetMapping("/all")
    public ResponseEntity<BaseResponse<PageResponse<OrganizerFestivalListResponse>>> getMyFestivals(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<OrganizerFestivalListResponse> response = organizerFestivalService.getMyFestivals(pageable);
        return ResponseEntity
                .status(SuccessStatus.GET_MY_ALL_FESTIVALS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.GET_MY_ALL_FESTIVALS.getMsg(), response));
    }

    @Operation(summary = "진행중, 진행 예정 공연 조회", description = "주최사가 등록한 모든 공연 중 진행 중이거나 예정인 공연 조회")
    @ApiErrorCodes(SwaggerResponseDescription.NO_AUTHORIZATION)
    @GetMapping("/active")
    public ResponseEntity<BaseResponse<OrganizerActiveFestivalPageResponse>> getActiveFestivals(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        OrganizerActiveFestivalPageResponse response = organizerFestivalService.getActiveFestivals(pageable);
        return ResponseEntity
                .status(SuccessStatus.GET_MY_ALL_ACTIVE_FESTIVALS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.GET_MY_ALL_ACTIVE_FESTIVALS.getMsg(), response));
    }

}
