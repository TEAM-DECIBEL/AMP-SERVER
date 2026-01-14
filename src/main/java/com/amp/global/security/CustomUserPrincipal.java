package com.amp.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserPrincipal extends User {

    private final Long userId;
    private final String email;

    public CustomUserPrincipal(Long userId, String email,
                               Collection<? extends GrantedAuthority> authorities) {
        super(email, "", authorities);
        this.userId = userId;
        this.email = email;
    }
}
