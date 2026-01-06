package com.bkap.teach.repository.teacher;

import com.bkap.teach.entity.TeacherGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherGradeRepository extends JpaRepository<TeacherGrade, Long> {

    List<TeacherGrade> findByTeacherId(Long teacherId);

    void deleteByTeacherIdAndGrade(Long teacherId, Integer grade);

    boolean existsByTeacherIdAndGrade(Long teacherId, Integer grade);

}