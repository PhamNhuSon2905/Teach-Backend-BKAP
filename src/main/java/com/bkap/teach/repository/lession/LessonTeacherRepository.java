package com.bkap.teach.repository.lession;

import com.bkap.teach.entity.Lesson;
import com.bkap.teach.entity.LessonTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonTeacherRepository extends JpaRepository<LessonTeacher,Long> {
    List<LessonTeacher> findByLessonId(Long lessonId);
    void deleteByLessonId(Long lessonId);
    boolean existsByLessonIdAndTeacherId(Long lessonId, Long teacherId);

    List<LessonTeacher> findByTeacherId(Long teacherId);

    List<LessonTeacher> findByTeacherIdAndLesson_Grade(
            Long teacherId,
            Integer grade
    );

}
