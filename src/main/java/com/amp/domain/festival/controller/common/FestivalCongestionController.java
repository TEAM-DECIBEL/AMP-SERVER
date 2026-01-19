package com.amp.domain.festival.controller.common;

import com.amp.domain.stage.dto.response.FestivalCongestionResponse;
import com.amp.domain.stage.service.CongestionQueryService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/common/festivals")
@RequiredArgsConstructor
public class FestivalCongestionController {

    private final CongestionQueryService congestionQueryService;

    @Operation(summary = "공연 별 전체 혼잡도 조회")
    @GetMapping("/{festivalId}/congestion")
    public ResponseEntity<BaseResponse<FestivalCongestionResponse>> getFestivalCongestion(@PathVariable @Positive Long festivalId,
                                                                                          Pageable pageable) {
        FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(festivalId, pageable);

        SuccessStatus status = response.stages().isEmpty()
                ? SuccessStatus.CONGESTION_LIST_EMPTY
                : SuccessStatus.CONGESTION_GET_SUCCESS;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }
}
