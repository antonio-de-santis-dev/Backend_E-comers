package com.it.userservis.dto;

import com.it.userservis.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTOOutput {

    private UUID accountId;

    private UUID userId;

    private String username;

    private Role role;

    private String token;
}
