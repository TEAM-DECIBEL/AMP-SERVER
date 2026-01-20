package com.amp.domain.user.entity;

public enum RegistrationStatus {
    /**
     * OAuth2 로그인 직후 (userType 선택 전)
     */
    PENDING,

    /**
     * userType은 설정되었으나 이름 입력이 필요한 상태
     */
    NAME_PENDING,

    /**
     * 모든 온보딩 완료
     */
    COMPLETED
}