package com.pipeline.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Error response DTO")
public class ErrorResponse {
    @Schema(description = "Error type", example = "Authentication Error")
    private String error;
    @Schema(description = "Detailed error message", example = "Email already registered, please login.")
    private String message;
    @Schema(description = "HTTP status code", example = "401")
    private int status;
    @Schema(description = "Timestamp of the error", example = "2023-10-01T12:00:00")
    private java.time.LocalDateTime timestamp;
}
