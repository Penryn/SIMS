package com.graduate.management.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graduate.management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String name;
    @JsonIgnore
    private String password;
    private String email;
    private String phone;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Boolean firstLogin;
    private Collection<? extends GrantedAuthority> authorities;
    
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getEnabled(),
                user.getAccountNonLocked(),
                user.getFirstLogin(),
                authorities);
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
