package com.bkap.teach.repository.teacher;

import com.bkap.teach.entity.TeacherGradeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherGradeHistoryRepository extends JpaRepository<TeacherGradeHistory, Long> {

    Page<TeacherGradeHistory>
    findByTeacherIdOrderByActionAtDesc(Long teacherId, Pageable pageable);
}
