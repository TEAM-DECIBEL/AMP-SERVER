package com.amp.domain.stage.controller;

import com.amp.domain.stage.dto.request.CongestionReportRequest;
import com.amp.domain.stage.service.CongestionReportService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/stages/{stageId}/congestion")
@Tag(name = "User API")
public class StageCongestionController {

    private final CongestionReportService congestionReportService;

    @PostMapping
    @Operation(summary = "현장 혼잡도 입력")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_INPUT_CONGESTION)
    public ResponseEntity<BaseResponse<Void>> reportCongestion(
            @PathVariable @Positive Long stageId,
            @RequestBody @Valid CongestionReportRequest request) {

        congestionReportService.reportCongestion(stageId, request.congestionLevel());

        return ResponseEntity
                .status(SuccessStatus.CONGESTION_INPUT_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.CONGESTION_INPUT_SUCCESS.getMsg(), null));
    }

}
