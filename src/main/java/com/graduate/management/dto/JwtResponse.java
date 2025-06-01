package com.graduate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String refreshToken;
    private Long id;
    private String username;
    private String name;
    private boolean firstLogin;
    private LocalDateTime expireAt;
    private LocalDateTime refreshExpireAt;
}
