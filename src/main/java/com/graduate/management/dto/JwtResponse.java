package com.graduate.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "JWT认证响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    @Schema(description = "用户姓名", example = "管理员")
    private String name;
    
    @Schema(description = "是否首次登录", example = "false")
    private boolean firstLogin;
    
    @Schema(description = "访问令牌过期时间")
    private LocalDateTime expireAt;
    
    @Schema(description = "刷新令牌过期时间")
    private LocalDateTime refreshExpireAt;
}
