package com.amp.domain.stage.controller;

import com.amp.domain.stage.dto.request.CongestionReportRequest;
import com.amp.domain.stage.service.CongestionReportService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/stages/{stageId}/congestion")
public class StageCongestionController {

    private final CongestionReportService congestionReportService;

    @PostMapping
    @Operation(summary = "현장 혼잡도 입력")
    public ResponseEntity<BaseResponse<Void>> repostCongestion(
            @PathVariable Long stageId,
            @RequestBody @Valid CongestionReportRequest request) {

        congestionReportService.reportCongestion(stageId, request.congestionLevel());

        return ResponseEntity
                .status(SuccessStatus.CONGESTION_INPUT_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.CONGESTION_INPUT_SUCCESS.getMsg(), null));
    }

}
