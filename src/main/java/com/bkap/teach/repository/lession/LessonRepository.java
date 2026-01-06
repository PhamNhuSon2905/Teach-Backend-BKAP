package com.bkap.teach.repository.lession;

import com.bkap.teach.entity.Lesson;
import com.bkap.teach.enums.LessonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    long count();
    Page<Lesson> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name, Pageable pageable);
    List<Lesson> findByGradeInAndLessonStatus(
            List<Integer> grades,
            LessonStatus lessonStatus
    );
}
