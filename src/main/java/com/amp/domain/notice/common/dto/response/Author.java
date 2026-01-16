package com.amp.domain.notice.common.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Author {
    private final Long userId;
    private final String nickname;

    @Builder
    public Author(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
