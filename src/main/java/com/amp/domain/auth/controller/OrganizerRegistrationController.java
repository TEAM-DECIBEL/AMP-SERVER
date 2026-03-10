package com.amp.domain.auth.controller;

import com.amp.domain.auth.dto.OrganizerRegistrationStatusResponse;
import com.amp.domain.auth.dto.VerifyRegistrationCodeRequest;
import com.amp.domain.auth.dto.VerifyRegistrationCodeResponse;
import com.amp.domain.auth.service.OrganizerRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/registration")
@Tag(name = "Auth")
@RequiredArgsConstructor
@Slf4j
public class OrganizerRegistrationController {

    private final OrganizerRegistrationService organizerRegistrationService;

    @GetMapping("/status")
    @Operation(summary = "Organizer 가입코드 검증 상태 확인")
    public ResponseEntity<OrganizerRegistrationStatusResponse> getRegistrationStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        log.info("Checking registration status for email: {}", email);

        OrganizerRegistrationStatusResponse status = organizerRegistrationService.getRegistrationStatus(email);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/verify")
    @Operation(summary = "Organizer 가입코드 검증")
    public ResponseEntity<VerifyRegistrationCodeResponse> verifyRegistrationCode(
            @Valid @RequestBody VerifyRegistrationCodeRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        log.info("Verifying registration code for email: {}", email);

        VerifyRegistrationCodeResponse response = organizerRegistrationService.verifyRegistrationCode(email, request);
        return ResponseEntity.ok(response);
    }
}
