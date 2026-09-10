package com.it.userservis.dto;


import com.it.userservis.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountDTOOutput {

    private UUID id;

    private UUID userId;

    private String username;

    private Role role;

}
