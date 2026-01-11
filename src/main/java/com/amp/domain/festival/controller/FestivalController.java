package com.amp.domain.festival.controller;

import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.service.FestivalService;
import com.amp.global.response.success.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vi/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    public BaseResponse<FestivalCreateResponse> createFestival(
            @RequestPart("request") @Valid FestivalCreateRequest request,
            @RequestPart("image") MultipartFile image) {

        FestivalCreateResponse response = festivalService.createFestival(request);
        return BaseResponse.create("dd", response);
    }

}
