package com.amp.domain.festival.dto.request;

import com.amp.domain.stage.dto.request.StageRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class FestivalCreateRequest {

    @NotBlank(message = "공연명은 필수입니다.")
    private String title;

    @NotBlank(message = "공연 장소는 필수입니다.")
    private String location;

    @NotBlank(message = "시작 날짜는 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotBlank(message = "종료 날짜는 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private MultipartFile image;

    private List<ScheduleRequest> schedules;

    private List<StageRequest> stages;

    private List<Long> selectedCategoryIds;
}
