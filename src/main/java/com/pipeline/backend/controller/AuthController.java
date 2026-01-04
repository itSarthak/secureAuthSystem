package com.pipeline.backend.controller;

import com.pipeline.backend.dto.SignupInitialRequestDTO;
import com.pipeline.backend.dto.SignupInitialResponseDTO;
import com.pipeline.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/initiate")
    public ResponseEntity<SignupInitialResponseDTO> initiateSignup(@Valid @RequestBody SignupInitialRequestDTO requestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.initiateSignup(requestDTO));
    }
}
