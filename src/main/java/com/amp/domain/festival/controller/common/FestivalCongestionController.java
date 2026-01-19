package com.amp.domain.festival.controller.common;

import com.amp.domain.stage.dto.response.FestivalCongestionResponse;
import com.amp.domain.stage.service.CongestionQueryService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/common/festivals")
@Tag(name = "User API")
@Tag(name = "Organizer API")
@RequiredArgsConstructor
public class FestivalCongestionController {

    private final CongestionQueryService congestionQueryService;

    @Operation(summary = "공연 별 전체 혼잡도 조회")
    @GetMapping("/{festivalId}/congestion")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_CONGESTION)
    public ResponseEntity<BaseResponse<FestivalCongestionResponse>> getFestivalCongestion(
            @PathVariable @Positive Long festivalId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(festivalId, pageable);

        SuccessStatus status = response.stages().isEmpty()
                ? SuccessStatus.CONGESTION_LIST_EMPTY
                : SuccessStatus.CONGESTION_GET_SUCCESS;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }
}
