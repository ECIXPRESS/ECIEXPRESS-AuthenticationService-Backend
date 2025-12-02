package edu.dosw.dto;

import edu.dosw.model.enums.Role;

public record UserCredentialsDto(
        String id,
        String userId,
        String email,
        String password,
        Role role,
        String pfpURL
) {}