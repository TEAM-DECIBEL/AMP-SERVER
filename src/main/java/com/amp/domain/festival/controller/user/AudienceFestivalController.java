package com.amp.domain.festival.controller.user;

import com.amp.domain.festival.dto.response.AudienceFestivalSummaryResponse;
import com.amp.domain.festival.service.user.AudienceFestivalService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.common.dto.PageResponse;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/users/festivals")
@Tag(name = "User API")
@RequiredArgsConstructor
public class AudienceFestivalController {
    private final AudienceFestivalService audienceFestivalService;

    @GetMapping
    @Operation(summary = "전체 공연 목록 조회")
    public ResponseEntity<BaseResponse<PageResponse<AudienceFestivalSummaryResponse>>> getAllFestivals(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AudienceFestivalSummaryResponse> response = audienceFestivalService.getAllFestivals(pageable);

        SuccessStatus status = response.festivals().isEmpty()
                ? SuccessStatus.FESTIVAL_LIST_EMPTY
                : SuccessStatus.FESTIVAL_LIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

}
