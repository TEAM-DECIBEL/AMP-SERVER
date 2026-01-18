package com.amp.domain.festival.controller.organizer;

import com.amp.domain.festival.dto.response.OrganizerActiveFestivalPageResponse;
import com.amp.domain.festival.dto.response.OrganizerFestivalPageResponse;
import com.amp.domain.festival.service.organizer.OrganizerFestivalService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizer/me/festivals")
@RequiredArgsConstructor
public class OrganizerFestivalController {

    private final OrganizerFestivalService organizerFestivalService;

    @Operation(summary = "진행한 모든 공연 조회", description = "주최사가 등록한 모든 공연 중 삭제된 것을 제외하고 전부 조회")
    @GetMapping("/all")
    public ResponseEntity<BaseResponse<OrganizerFestivalPageResponse>> getMyFestivals(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrganizerFestivalPageResponse response = organizerFestivalService.getMyFestivals(pageable);
        return ResponseEntity
                .status(SuccessStatus.GET_MY_ALL_FESTIVALS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.GET_MY_ALL_FESTIVALS.getMsg(), response));
    }

    @Operation(summary = "진행중, 진행 예정 공연 조회", description = "주최사가 등록한 모든 공연 중 진행 중이거나 예정인 공연 조회")
    @GetMapping("/active")
    public ResponseEntity<BaseResponse<OrganizerActiveFestivalPageResponse>> getActiveFestivals(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrganizerActiveFestivalPageResponse response = organizerFestivalService.getActiveFestivals(pageable);
        return ResponseEntity
                .status(SuccessStatus.GET_MY_ALL_ACTIVE_FESTIVALS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.GET_MY_ALL_ACTIVE_FESTIVALS.getMsg(), response));
    }

}
