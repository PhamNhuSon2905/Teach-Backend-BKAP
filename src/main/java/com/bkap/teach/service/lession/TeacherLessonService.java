package com.bkap.teach.service.lession;

import com.bkap.teach.entity.Lesson;
import com.bkap.teach.entity.LessonTeacher;
import com.bkap.teach.entity.TeacherGrade;
import com.bkap.teach.enums.LessonStatus;
import com.bkap.teach.repository.lession.LessonFileRepository;
import com.bkap.teach.repository.lession.LessonRepository;
import com.bkap.teach.repository.lession.LessonTeacherRepository;
import com.bkap.teach.repository.teacher.TeacherGradeRepository;
import com.bkap.teach.teacher.dto.response.LessonFileResponse;
import com.bkap.teach.teacher.dto.response.TeacherLessonContentResponse;
import com.bkap.teach.teacher.dto.response.TeacherLessonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;


@Service
public class TeacherLessonService {

    @Autowired
    private LessonTeacherRepository lessonTeacherRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TeacherGradeRepository teacherGradeRepository;

    @Autowired
    private LessonFileRepository lessonFileRepository;


    public List<TeacherLessonResponse> teacherLessonByAssignedGrades(Long teacherId, String keyword, Integer grade, Integer teachingMonth) {
        List<Integer> assignedGrades =
                teacherGradeRepository.findByTeacherId(teacherId)
                        .stream()
                        .map(TeacherGrade::getGrade)
                        .toList();
        if (assignedGrades.isEmpty()) {
            return List.of();
        }

        List<Integer> gradesToQuery = (grade == null)
                        ? assignedGrades
                        : assignedGrades.contains(grade)
                        ? List.of(grade)
                        : List.of();

        if (gradesToQuery.isEmpty()) {
            return List.of();
        }

        List<Lesson> lessons =
                lessonRepository.findByGradeInAndLessonStatus(
                        gradesToQuery,
                        LessonStatus.ACTIVE
                );

        if (teachingMonth != null) {
            lessons = lessons.stream()
                    .filter(l -> teachingMonth.equals(l.getTeachingMonth()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            lessons = lessons.stream()
                    .filter(l ->
                            l.getCode().toLowerCase().contains(kw)
                                    || l.getName().toLowerCase().contains(kw)
                    )
                    .toList();
        }

        return lessons.stream()
                .map(this::toResponse)
                .toList();
    }


    public List<TeacherLessonResponse> teacherAssignedLessonList(
            Long teacherId,
            String keyword,
            Integer grade
    ) {

        List<LessonTeacher> assignments =
                (grade == null)
                        ? lessonTeacherRepository.findByTeacherId(teacherId)
                        : lessonTeacherRepository.findByTeacherIdAndLesson_Grade(
                        teacherId,
                        grade
                );

        List<Lesson> lessons = assignments.stream()
                .map(LessonTeacher::getLesson)
                .filter(l -> l.getLessonStatus() == LessonStatus.ACTIVE)
                .toList();

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            lessons = lessons.stream()
                    .filter(l ->
                            l.getCode().toLowerCase().contains(kw)
                                    || l.getName().toLowerCase().contains(kw)
                    )
                    .toList();
        }

        return lessons.stream()
                .map(this::toResponse)
                .toList();
    }

    public TeacherLessonContentResponse getLessonContent(
            Long lessonId,
            Long teacherId
    ) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại !"));

        boolean hasPermission =
                lessonTeacherRepository.existsByLessonIdAndTeacherId(lessonId, teacherId)
                        || teacherGradeRepository
                        .findByTeacherId(teacherId)
                        .stream()
                        .anyMatch(g -> g.getGrade().equals(lesson.getGrade()));

        if (!hasPermission) {
            throw new RuntimeException("Không có quyền xem bài giảng này !");
        }

        List<LessonFileResponse> files =
                lessonFileRepository.findByLessonId(lessonId)
                        .stream()
                        .map(f -> {
                            LessonFileResponse r = new LessonFileResponse();
                            r.setId(f.getId());
                            r.setFileType(f.getFileType().name());
                            r.setFileName(f.getFileName());
                            r.setFilePath(f.getFilePath());
                            r.setFolderPath(f.getFolderPath());
                            r.setFileSize(f.getFileSize());
                            r.setIsRoot(f.getIsRoot());
                            return r;
                        })
                        .toList();

        TeacherLessonContentResponse res = new TeacherLessonContentResponse();
        res.setId(lesson.getId());
        res.setCode(lesson.getCode());
        res.setName(lesson.getName());
        res.setGrade(lesson.getGrade());
        res.setTeachingMonth(lesson.getTeachingMonth());
        res.setDescription(lesson.getDescription());
        res.setCoverImage(lesson.getCoverImage());
        res.setFiles(files);

        return res;
    }


    private TeacherLessonResponse toResponse(Lesson lesson) {
        TeacherLessonResponse res = new TeacherLessonResponse();
        res.setId(lesson.getId());
        res.setCode(lesson.getCode());
        res.setName(lesson.getName());
        res.setGrade(lesson.getGrade());
        res.setTeachingMonth(lesson.getTeachingMonth());
        res.setCoverImage(lesson.getCoverImage());
        return res;
    }
}
