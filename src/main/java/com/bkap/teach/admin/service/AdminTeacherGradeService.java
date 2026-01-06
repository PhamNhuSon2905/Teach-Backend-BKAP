package com.bkap.teach.admin.service;

import com.bkap.teach.entity.TeacherGrade;
import com.bkap.teach.entity.TeacherGradeHistory;
import com.bkap.teach.entity.User;
import com.bkap.teach.enums.TeacherGradeAction;
import com.bkap.teach.repository.UserRepository;
import com.bkap.teach.repository.teacher.TeacherGradeHistoryRepository;
import com.bkap.teach.repository.teacher.TeacherGradeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTeacherGradeService {

    @Autowired
    private TeacherGradeRepository teacherGradeRepository;

    @Autowired
    private TeacherGradeHistoryRepository teacherGradeHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Integer> getAssignedGrades(Long teacherId) {

        if (!userRepository.existsById(teacherId)) {
            throw new RuntimeException("Giảng viên không tồn tại !");
        }

        return teacherGradeRepository
                .findByTeacherId(teacherId)
                .stream()
                .map(TeacherGrade::getGrade)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignGrades(
            Long teacherId,
            List<Integer> newGrades,
            User currentAdmin
    ) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Giảng viên không tồn tại !"));

        List<Integer> oldGrades = teacherGradeRepository
                .findByTeacherId(teacherId)
                .stream()
                .map(TeacherGrade::getGrade)
                .toList();

        if (newGrades == null) {
            newGrades = List.of();
        }

        for (Integer old : oldGrades) {
            if (!newGrades.contains(old)) {
                teacherGradeRepository
                        .deleteByTeacherIdAndGrade(teacherId, old);

                saveHistory(
                        teacher,
                        currentAdmin,
                        old,
                        TeacherGradeAction.UNASSIGN
                );
            }
        }

        for (Integer grade : newGrades) {

            if (grade < 1 || grade > 12) {
                throw new RuntimeException("Khối học không hợp lệ : " + grade);
            }

            if (!oldGrades.contains(grade)) {
                TeacherGrade tg = new TeacherGrade();
                tg.setTeacher(teacher);
                tg.setGrade(grade);
                teacherGradeRepository.save(tg);

                saveHistory(
                        teacher,
                        currentAdmin,
                        grade,
                        TeacherGradeAction.ASSIGN
                );
            }
        }
    }

    private void saveHistory(
            User teacher,
            User admin,
            Integer grade,
            TeacherGradeAction action
    ) {
        TeacherGradeHistory history = new TeacherGradeHistory();
        history.setTeacher(teacher);
        history.setPerformedBy(admin);
        history.setGrade(grade);
        history.setAction(action);
        // actionAt tự set trong @PrePersist

        teacherGradeHistoryRepository.save(history);
    }


}
