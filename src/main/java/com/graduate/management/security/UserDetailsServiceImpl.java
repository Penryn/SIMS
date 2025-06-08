package com.graduate.management.security;

import com.graduate.management.entity.User;
import com.graduate.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Value("${system.password.expired-days:90}")
    private int passwordExpiredDays;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("未找到用户: " + username));
        
        // 检查密码是否过期
        checkPasswordExpiration(user);
        
        return UserDetailsImpl.build(user);
    }
    
    /**
     * 检查密码是否过期
     * 根据需求，密码90天需要更换一次
     * 
     * @param user 用户对象
     */
    private void checkPasswordExpiration(User user) {
        LocalDateTime lastPasswordChangeTime = user.getLastPasswordChangeTime();
        
        // 如果从未修改过密码或最后修改密码时间为空，设置为账号创建时间
        if (lastPasswordChangeTime == null) {
            lastPasswordChangeTime = user.getCreatedAt();
            user.setLastPasswordChangeTime(lastPasswordChangeTime);
            userRepository.save(user);
        }
        
        // 计算密码已使用天数
        long daysElapsed = ChronoUnit.DAYS.between(lastPasswordChangeTime, LocalDateTime.now());
        
        // 如果密码已使用天数超过配置的过期天数，标记为需要修改密码
        if (daysElapsed >= passwordExpiredDays) {
            user.setFirstLogin(true); // 使用firstLogin字段来标记需要修改密码
            userRepository.save(user);
        }
    }
}
