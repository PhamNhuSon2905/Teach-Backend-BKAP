package com.bkap.teach.admin.service;

import com.bkap.teach.entity.Lesson;
import com.bkap.teach.entity.LessonTeacher;
import com.bkap.teach.entity.User;
import com.bkap.teach.repository.lession.LessonRepository;
import com.bkap.teach.repository.lession.LessonTeacherRepository;
import com.bkap.teach.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminLessonTeacherService {

    @Autowired
    private LessonTeacherRepository lessonTeacherRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Long> getAssignedTeacherIds(Long lessonId) {
        return lessonTeacherRepository
                .findByLessonId(lessonId)
                .stream()
                .map(lt -> lt.getTeacher().getId())
                .toList();
    }

    @Transactional
    public void assignTeachers(Long lessonId, List<Long> teacherIds) {

        lessonTeacherRepository.deleteByLessonId(lessonId);
        if (teacherIds == null || teacherIds.isEmpty()) {
            return;
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() ->
                        new RuntimeException("Bài giảng không tồn tại !"));

        for (Long teacherId : teacherIds) {
            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() ->
                            new RuntimeException("Giảng viên không tồn tại !"));

            LessonTeacher lt = new LessonTeacher();
            lt.setLesson(lesson);
            lt.setTeacher(teacher);

            lessonTeacherRepository.save(lt);
        }
    }
}
