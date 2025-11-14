package edu.dosw.application.dto;

import edu.dosw.domain.model.enums.Role;

public record AuthResponseDTO(
        String token,
        String userId,
        String email,
        Role role
) {}