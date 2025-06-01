package com.graduate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    private String name;
    
    private String email;
    
    private String phone;
    
    private List<String> roles;
    
    private Boolean enabled;
    
    private Boolean accountNonLocked;
    
    private Boolean firstLogin;
}
