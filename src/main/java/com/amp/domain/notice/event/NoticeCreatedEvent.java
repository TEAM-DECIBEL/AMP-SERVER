package com.amp.domain.notice.event;

import com.amp.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NoticeCreatedEvent {

    private final Long categoryId;
    private final String categoryName;

    private final String festivalName;
    private final Notice notice;
    private final String title;
    private final LocalDateTime createdAt;
}

