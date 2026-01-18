package com.amp.domain.festival.controller.common;

import com.amp.domain.stage.dto.response.FestivalCongestionResponse;
import com.amp.domain.stage.service.CongestionQueryService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/v1/festivals")
@RequiredArgsConstructor
public class FestivalCongestionController {

    private final CongestionQueryService congestionQueryService;

    @GetMapping("/{festivalId}/congestion")
    public ResponseEntity<BaseResponse<FestivalCongestionResponse>> getFestivalCongestion(@PathVariable Long festivalId,
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
