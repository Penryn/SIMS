package com.graduate.management.service.impl;

import com.graduate.management.dto.JwtResponse;
import com.graduate.management.dto.LoginRequest;
import com.graduate.management.dto.PasswordChangeRequest;
import com.graduate.management.dto.RegisterRequest;
import com.graduate.management.dto.UserDto;
import com.graduate.management.entity.Role;
import com.graduate.management.entity.User;
import com.graduate.management.repository.RoleRepository;
import com.graduate.management.repository.UserRepository;
import com.graduate.management.security.JwtTokenProvider;
import com.graduate.management.security.UserDetailsImpl;
import com.graduate.management.service.SystemLogService;
import com.graduate.management.service.UserService;
import com.graduate.management.util.SM3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
      private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SM3Util sm3Util;
    private final SystemLogService systemLogService;
    
    @Value("${system.password.expired-days}")
    private int passwordExpiredDays;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;
    
    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getUsername());
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 更新最后登录时间
        updateLastLoginTime(userDetails.getUsername());
        
        // 重置登录尝试次数
        updateLoginAttempts(userDetails.getUsername(), 0);
        
        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .name(userDetails.getName())
                .firstLogin(userDetails.getFirstLogin())
                .expireAt(LocalDateTime.now().plus(jwtExpiration, ChronoUnit.MILLIS))
                .refreshExpireAt(LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS))
                .build();
    }
    
    @Override
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("刷新令牌已过期或无效");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String newToken = jwtTokenProvider.generateToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
          return JwtResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .firstLogin(user.getFirstLogin())
                .expireAt(LocalDateTime.now().plus(jwtExpiration, ChronoUnit.MILLIS))
                .refreshExpireAt(LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS))
                .build();
    }
    
    @Override
    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        // 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        // 设置角色
        Role role = roleRepository.findByName(registerRequest.getRole())
                .orElseThrow(() -> new RuntimeException("角色不存在: " + registerRequest.getRole()));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        // 设置初始状态
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFirstLogin(false); // 注册用户不需要强制修改密码
        user.setLastPasswordChangeTime(LocalDateTime.now());
        user.setLoginAttempts(0);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 记录注册日志
        systemLogService.log("REGISTER", "USER", savedUser.getId(), null,
                "用户注册: " + savedUser.getUsername() + " (" + registerRequest.getRole() + ")", 
                true, null, null);
        
        // 自动登录新注册的用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(registerRequest.getUsername());
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .name(userDetails.getName())
                .firstLogin(userDetails.getFirstLogin())
                .expireAt(LocalDateTime.now().plus(jwtExpiration, ChronoUnit.MILLIS))
                .refreshExpireAt(LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS))
                .build();
    }
      @Override
    @Transactional
    public User createUser(User user) {
        // 如果是学生，默认密码为身份证号后8位
        String defaultPassword = user.getPassword();
        if (defaultPassword == null || defaultPassword.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        
        // 使用SM3PasswordEncoder进行密码编码（包含盐值）
        user.setPassword(passwordEncoder.encode(defaultPassword));
        
        // 设置初始登录状态
        user.setFirstLogin(true);
        user.setLastPasswordChangeTime(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setEnabled(user.getEnabled());
        
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existingUser.setRoles(user.getRoles());
        }
        
        return userRepository.save(existingUser);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDto(user);
    }
    
    @Override
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDto(user);
    }
    
    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    @Override
    public List<UserDto> getUsersByRole(String role) {
        Role roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        return roleEntity.getUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
      @Override
    @Transactional
    public boolean changePassword(String username, PasswordChangeRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证旧密码（使用PasswordEncoder）
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        
        // 使用SM3PasswordEncoder加密新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordChangeTime(LocalDateTime.now());
        user.setFirstLogin(false);
        
        userRepository.save(user);
        return true;
    }
      @Override
    @Transactional
    public boolean resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 重置密码为默认密码（身份证号后8位）
        // 在实际应用中，应该有一个安全的方式来重置密码
        String defaultPassword = "12345678"; // 这里只是示例，实际应用需要更安全的处理
        
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setFirstLogin(true);
        user.setLastPasswordChangeTime(LocalDateTime.now());
        
        userRepository.save(user);
        return true;
    }
    
    @Override
    @Transactional
    public boolean lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setAccountNonLocked(false);
        user.setLockedTime(LocalDateTime.now());
        
        userRepository.save(user);
        return true;
    }
    
    @Override
    @Transactional
    public boolean unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setAccountNonLocked(true);
        user.setLockedTime(null);
        user.setLoginAttempts(0);
        
        userRepository.save(user);
        return true;
    }
    
    @Override
    @Transactional
    public boolean enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(true);
        
        userRepository.save(user);
        return true;
    }
    
    @Override
    @Transactional
    public boolean disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEnabled(false);
        
        userRepository.save(user);
        return true;
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public boolean checkPasswordExpiry(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getLastPasswordChangeTime() == null) {
            return true; // 需要修改密码
        }
        
        long daysSinceLastChange = ChronoUnit.DAYS.between(user.getLastPasswordChangeTime(), LocalDateTime.now());
        
        return daysSinceLastChange >= passwordExpiredDays;
    }
    
    @Override
    @Transactional
    public void updateLoginAttempts(String username, int attempts) {
        userRepository.updateLoginAttempts(attempts, username);
    }
    
    @Override
    @Transactional
    public void updateAccountLockStatus(String username, boolean locked) {
        userRepository.updateAccountLockStatus(!locked, locked ? LocalDateTime.now() : null, username);
    }
    
    @Override
    @Transactional
    public void updateLastLoginTime(String username) {
        userRepository.updateLastLoginTime(LocalDateTime.now(), username);
    }
      private final com.graduate.management.util.DtoMaskUtil dtoMaskUtil;
    
    private UserDto convertToDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .enabled(user.getEnabled())
                .accountNonLocked(user.getAccountNonLocked())
                .firstLogin(user.getFirstLogin())
                .build();
                
        // 对DTO进行脱敏处理
        return dtoMaskUtil.maskUserDto(userDto);
    }
}
