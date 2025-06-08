package com.graduate.management.repository;

import com.graduate.management.entity.SystemLog;
import com.graduate.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    
    List<SystemLog> findByUser(User user);
    
    List<SystemLog> findByOperation(String operation);
      List<SystemLog> findByResourceType(String resourceType);
    
    List<SystemLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId);
    
    /**
     * 根据创建时间区间查询日志
     * 用于日志完整性校验
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 在时间区间内的日志列表
     */
    List<SystemLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    Page<SystemLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT l FROM SystemLog l WHERE " +
           "(l.user.username LIKE %?1% OR l.operation LIKE %?1% OR l.resourceType LIKE %?1%) " +
           "AND l.createdAt BETWEEN ?2 AND ?3")
    Page<SystemLog> searchLogs(String keyword, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
