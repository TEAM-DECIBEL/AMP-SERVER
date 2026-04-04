package com.amp.domain.auth.dto;

import com.amp.domain.user.entity.UserType;

public record AuthStatusResponse(
        boolean authenticated,
        String email,
        UserType userType
) {
    public static AuthStatusResponse loggedIn(String email, UserType userType) {
        return new AuthStatusResponse(true, email, userType);
    }

    public static AuthStatusResponse loggedOut() {
        return new AuthStatusResponse(false, null, null);
    }
}
