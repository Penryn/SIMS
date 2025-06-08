package com.graduate.management.config;

import com.graduate.management.entity.SystemLog;
import com.graduate.management.repository.SystemLogRepository;
import com.graduate.management.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 日志完整性校验定时任务
 * 实现需求中的：采用基于国产密码算法对日志记录进行完整性保护
 * 定期比对日志记录和HMAC值，确保日志完整性
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class LogIntegrityScheduler {
     
    private final SystemLogRepository systemLogRepository;
    private final SystemLogService systemLogService;
    
    @Value("${system.log.hmac-key:logSecurityKey}")
    private String hmacKey;
      /**
     * 每天凌晨2点执行日志完整性校验
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(readOnly = true)
    public void verifyLogIntegrity() {
        log.info("开始执行日志完整性校验...");
        
        try {
            // 查询过去24小时的所有日志
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(1);
            
            List<SystemLog> logs = systemLogRepository.findByCreatedAtBetween(startTime, endTime);
            log.info("找到{}条日志记录需要校验", logs.size());
            
            int integrityFailureCount = 0;
            List<Long> tamperLogIds = new ArrayList<>();
            
            for (SystemLog logEntry : logs) {
                if (!verifyLogEntryIntegrity(logEntry)) {
                    integrityFailureCount++;
                    tamperLogIds.add(logEntry.getId());
                    log.warn("日志ID: {} 完整性校验失败！", logEntry.getId());
                }
            }
            
            // 记录本次校验结果
            SystemLog verificationLog = new SystemLog();
            verificationLog.setOperation("LOG_INTEGRITY_CHECK");
            verificationLog.setResourceType("SYSTEM");
            verificationLog.setDetails("日志完整性校验：检查" + logs.size() + "条日志，发现" + 
                                       integrityFailureCount + "条不一致");
              if (integrityFailureCount > 0) {
                log.error("日志完整性校验失败! 总计{}条日志被篡改，ID: {}", 
                        integrityFailureCount, formatLogIds(tamperLogIds));
                
                verificationLog.setSuccess(false);
                verificationLog.setErrorMessage("发现篡改日志，ID: " + formatLogIds(tamperLogIds));
                
                // 可以添加发送警报邮件或其他通知方式
                sendAlertNotification(integrityFailureCount, tamperLogIds);
            } else {
                log.info("日志完整性校验成功！");
                verificationLog.setSuccess(true);
            }
            
            // 保存校验日志（不计算HMAC，避免递归）
            systemLogRepository.save(verificationLog);
            
        } catch (Exception e) {
            log.error("日志完整性校验过程中发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 验证单条日志记录的完整性
     *
     * @param logEntry 日志记录
     * @return 是否完整(未被篡改)
     */
    private boolean verifyLogEntryIntegrity(SystemLog logEntry) {
        if (logEntry.getHmacValue() == null || logEntry.getHmacValue().isEmpty()) {
            return true; // 跳过没有HMAC值的记录
        }
        
        // 重新计算HMAC值并与存储的值比较
        String calculatedHmac = systemLogService.calculateLogHmac(logEntry);
        return calculatedHmac.equals(logEntry.getHmacValue());
    }
    
    /**
     * 发送警报通知
     *
     * @param tamperedCount 被篡改的日志数量
     * @param logIds 被篡改的日志ID列表
     */    private void sendAlertNotification(int tamperedCount, List<Long> logIds) {
        // 实际应用中，可以集成邮件发送、短信通知或其他告警机制
        log.warn("发现日志完整性问题！共{}条日志可能被篡改，ID: {}", 
                tamperedCount, formatLogIds(logIds));
        
        // 这里只是记录日志，在实际应用中可以扩展为发送邮件或短信
        // mailService.sendAlert("日志完整性告警", "发现" + tamperedCount + "条日志可能被篡改");
    }
    
    /**
     * 格式化日志ID列表为字符串
     *
     * @param logIds 日志ID列表
     * @return 格式化后的字符串
     */
    private String formatLogIds(List<Long> logIds) {
        StringBuilder sb = new StringBuilder();
        for (Long id : logIds) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(id);
        }
        return sb.toString();
    }
}