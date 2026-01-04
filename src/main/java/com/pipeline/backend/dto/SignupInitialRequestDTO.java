package com.pipeline.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request DTO for initiating user signup")
public class SignupInitialRequestDTO {

    @Schema(description = "Full name of the user", example = "John Doe", minLength = 2, maxLength = 100)
    @NotBlank(message = "Name is required")
    @Size(min =2, max = 100, message= "Name must be between 2 to 100 characters")
    private String fullName;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;
}
