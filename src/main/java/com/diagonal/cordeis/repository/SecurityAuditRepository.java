package com.diagonal.cordeis.repository;

import com.diagonal.cordeis.security.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditRepository extends JpaRepository<SecurityAuditLog, Long> {
    
    List<SecurityAuditLog> findByUsernameAndTimestampAfter(String username, LocalDateTime after);
    
    List<SecurityAuditLog> findByActionAndSuccessFalseAndTimestampAfter(String action, LocalDateTime after);
    
    @Query("SELECT COUNT(s) FROM SecurityAuditLog s WHERE s.username = :username AND s.action = :action AND s.success = false AND s.timestamp > :after")
    long countFailedAttempts(@Param("username") String username, @Param("action") String action, @Param("after") LocalDateTime after);
    
    @Modifying
    @Query("DELETE FROM SecurityAuditLog s WHERE s.timestamp < :cutoff")
    void deleteOldLogs(@Param("cutoff") LocalDateTime cutoff);
}