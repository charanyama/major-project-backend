package com.virutualstore.user_service.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    private String name;

    @Size(max = 20, message = "Mobile must not exceed 20 characters")
    private String mobile;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}