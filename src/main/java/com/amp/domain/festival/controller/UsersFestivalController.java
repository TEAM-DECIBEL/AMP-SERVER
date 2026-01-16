package com.amp.domain.festival.controller;

import com.amp.domain.festival.service.UsersFestivalService;
import com.amp.domain.userFestival.dto.response.UserFestivalPageResponse;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/festivals")
@RequiredArgsConstructor
public class UsersFestivalController {
    private final UsersFestivalService usersFestivalService;

    @GetMapping
    public ResponseEntity<BaseResponse<UserFestivalPageResponse>> getAllFestivalLists(
            @PageableDefault(size = 20) Pageable pageable) {

        UserFestivalPageResponse response = usersFestivalService.getAllFestivalLists(pageable);

        SuccessStatus status = response.isEmpty()
                ? SuccessStatus.FESTIVAL_LIST_EMPTY
                : SuccessStatus.FESTIVAL_LIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

}
