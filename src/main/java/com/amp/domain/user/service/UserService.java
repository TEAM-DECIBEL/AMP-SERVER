package com.amp.domain.user.service;

import com.amp.domain.user.dto.MyPageResponse;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.amp.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public MyPageResponse getMyPage(Long userId){
        User user = userRepository.findById(userId).
                orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return MyPageResponse.from(user);
    }
}
