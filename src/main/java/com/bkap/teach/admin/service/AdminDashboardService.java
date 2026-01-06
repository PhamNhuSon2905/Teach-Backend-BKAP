package com.bkap.teach.admin.service;

import com.bkap.teach.enums.Role;
import com.bkap.teach.enums.Status;
import com.bkap.teach.repository.AuditLogRepository;
import com.bkap.teach.repository.UserRepository;
import com.bkap.teach.repository.lession.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    public long totalUsers() {
        return userRepository.count();
    }

    public long totalTeachers() {
        return userRepository.countByRole(Role.TEACHER);
    }

    public long activeUsers() {
        return userRepository.countByStatus(Status.ACTIVE);
    }

    public long lockedUsers() {
        return userRepository.countByStatus(Status.LOCKED);
    }

    public long totalLessons() {
        return lessonRepository.count();
    }

    public long totalAuditLogs() {
        return auditLogRepository.count();
    }
}
