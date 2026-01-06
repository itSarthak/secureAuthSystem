package com.pipeline.backend.controller;

import com.pipeline.backend.dto.*;
import com.pipeline.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/initiate")
    @Operation(summary = "Initiate user signup", description = "Starts the signup process by sending an OTP to the user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Signup initiated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignupInitialResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication error (e.g., email already registered)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SignupInitialResponseDTO> initiateSignup(@Valid @RequestBody SignupInitialRequestDTO requestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.initiateSignup(requestDTO));
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<String> completeSignup(@RequestBody SignupCompleteRequestDTO requestDTO) {
        // Implementation for completing signup
        return ResponseEntity.ok().body("Signup completed");
    }
}
