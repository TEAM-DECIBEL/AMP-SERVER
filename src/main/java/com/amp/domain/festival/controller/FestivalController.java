package com.amp.domain.festival.controller;

import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.service.FestivalService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<FestivalCreateResponse> createFestival
            (@ModelAttribute @Valid FestivalCreateRequest request) {
        FestivalCreateResponse response = festivalService.createFestival(request);
        return BaseResponse.create(SuccessStatus.FESTIVAL_CREATE_SUCCESS.getMsg(), response);
    }

}
