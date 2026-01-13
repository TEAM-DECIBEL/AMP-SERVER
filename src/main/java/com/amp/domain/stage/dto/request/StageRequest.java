package com.amp.domain.stage.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StageRequest {

    @NotBlank(message = "무대/부스 이름은 필수입니다.")
    private String title;

    private String location;

}
