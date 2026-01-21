package com.amp.domain.auth.controller;

import com.amp.domain.auth.dto.OnboardingRequest;
import com.amp.domain.auth.dto.OnboardingResponse;
import com.amp.domain.auth.dto.OnboardingStatusResponse;
import com.amp.domain.auth.service.OnboardingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/onboarding")
@Tag(name = "Auth API")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingService onboardingService;


    @PostMapping("/complete")
    public ResponseEntity<OnboardingResponse> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        log.info("Completing onboarding for user: {}, type: {}", email, request.getUserType());

        OnboardingResponse response = onboardingService.completeOnboarding(email, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusResponse> getOnboardingStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        OnboardingStatusResponse status = onboardingService.getOnboardingStatus(email);
        return ResponseEntity.ok(status);
    }

}
