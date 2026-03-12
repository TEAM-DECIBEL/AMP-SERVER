package com.amp.domain.notice.controller.organizer;

import com.amp.domain.notice.dto.request.NoticeCreateRequest;
import com.amp.domain.notice.dto.response.NoticeCreateResponse;
import com.amp.domain.notice.service.organizer.NoticeService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/festivals")
@Tag(name = "Notice")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ORGANIZER')")
public class NoticeCreateController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 생성")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_CREATE_NOTICE)
    @PostMapping(path = "/{festivalId}/notices",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<NoticeCreateResponse>> createNotice(
            @PathVariable @Positive Long festivalId,
            @RequestPart("noticeCreateRequest") @Valid NoticeCreateRequest noticeCreateRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        NoticeCreateResponse response = noticeService.createNotice(festivalId, noticeCreateRequest, images);
        return ResponseEntity
                .status(SuccessStatus.NOTICE_CREATE_SUCCESS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.NOTICE_CREATE_SUCCESS.getMsg(), response));
    }
}
