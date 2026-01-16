package com.amp.domain.wishList.dto.response;

import com.amp.domain.festival.common.entity.Festival;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentWishListResponse {

    private Long festivalId;
    private String title;
    private String mainImageUrl;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long dDay;

    public static RecentWishListResponse from(Festival festival) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), festival.getStartDate());

        return RecentWishListResponse.builder()
                .festivalId(festival.getId())
                .title(festival.getTitle())
                .mainImageUrl(festival.getMainImageUrl())
                .location(festival.getLocation())
                .startDate(festival.getStartDate())
                .endDate(festival.getEndDate())
                .dDay(dDay)
                .build();
    }

}