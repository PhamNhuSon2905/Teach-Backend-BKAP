package com.bkap.teach.admin.controller;

import com.bkap.teach.admin.dto.request.lession.CreateLessonRequest;
import com.bkap.teach.admin.dto.request.lession.UpdateLessonRequest;
import com.bkap.teach.admin.dto.request.lession.UploadLessonFileRequest;
import com.bkap.teach.admin.service.AdminLessonFileService;
import com.bkap.teach.admin.service.AdminLessonService;
import com.bkap.teach.audit.AuditAnotation;
import com.bkap.teach.common.notify.Notify;
import com.bkap.teach.entity.Lesson;
import com.bkap.teach.entity.LessonFile;
import com.bkap.teach.enums.Action;
import com.bkap.teach.enums.LessonStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/lesson")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminLessonController {

    @Autowired
    private AdminLessonService adminLessonService;
    @Autowired
    private AdminLessonFileService adminLessonFileService;



    @GetMapping("/index")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "LESSON_LIST"
    )
    public String index(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String keyword,
            Model model
    ) {

        var page = adminLessonService.findAll(pageNo, pageSize, keyword);

        model.addAttribute("lesson", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPage", page.getTotalPages());
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        return "admin/lesson/index";
    }


    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new CreateLessonRequest());
        return "admin/lesson/create";
    }



    @PostMapping("/create")
    @AuditAnotation(
            action = Action.CREATE,
            objectType = "LESSON"
    )
    public String create(
            @Valid @ModelAttribute("form") CreateLessonRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            Notify.warning(model, "Vui lòng kiểm tra lại thông tin bài giảng !");
            return "admin/lesson/create";
        }
        try {
            Lesson lesson = new Lesson();
            lesson.setCode(form.getCode());
            lesson.setName(form.getName());
            lesson.setGrade(form.getGrade());
            lesson.setTeachingMonth(form.getTeachingMonth());
            lesson.setDescription(form.getDescription());
            lesson.setLessonStatus(LessonStatus.ACTIVE);

            Lesson saved = adminLessonService.create(lesson);
            adminLessonService.handleCoverImage(saved, form.getCoverFile());

            Notify.success(
                    ra,
                    "Tạo thông tin bài giảng thành công: " + saved.getName()
            );

            return "redirect:/admin/lesson/index";

        } catch (RuntimeException e) {
            Notify.error(model, e.getMessage());
            return "admin/lesson/create";
        }
    }


    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {

        Lesson lesson = adminLessonService.findById(id);

        UpdateLessonRequest form = new UpdateLessonRequest();
        form.setCode(lesson.getCode());
        form.setName(lesson.getName());
        form.setGrade(lesson.getGrade());
        form.setTeachingMonth(lesson.getTeachingMonth());
        form.setDescription(lesson.getDescription());

        model.addAttribute("lessonId", id);
        model.addAttribute("form", form);
        model.addAttribute("currentCover", lesson.getCoverImage());

        return "admin/lesson/edit";
    }

    @PostMapping("/{id}/edit")
    @AuditAnotation(
            action = Action.UPDATE,
            objectType = "LESSON"
    )
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") UpdateLessonRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {

        if (bindingResult.hasErrors()) {
            Lesson lesson = adminLessonService.findById(id);
            model.addAttribute("lessonId", id);
            model.addAttribute("currentCover", lesson.getCoverImage());
            Notify.warning(model, "Vui lòng kiểm tra lại thông tin bài giảng!");
            return "admin/lesson/edit";
        }

        try {
            Lesson lesson = adminLessonService.findById(id);

            lesson.setCode(form.getCode());
            lesson.setName(form.getName());
            lesson.setGrade(form.getGrade());
            lesson.setTeachingMonth(form.getTeachingMonth());
            lesson.setDescription(form.getDescription());

            Lesson updated = adminLessonService.update(id, lesson);
            adminLessonService.handleCoverImage(updated, form.getCoverFile());

            Notify.success(
                    ra,
                    "Cập nhật bài giảng thành công: " + updated.getName()
            );

            return "redirect:/admin/lesson/index";

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
            return "redirect:/admin/lesson/index";
        }
    }


    @GetMapping("/{id}/content")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "LESSON_CONTENT"
    )
    public String content(@PathVariable Long id, Model model) {
        Lesson lesson = adminLessonService.findById(id);
        List<LessonFile> files = adminLessonFileService.findByLesson(id);

        model.addAttribute("lesson", lesson);
        model.addAttribute("files", files);
        model.addAttribute("uploadForm", new UploadLessonFileRequest());

        return "admin/lesson/content";
    }


    @PostMapping("/{id}/content")
    @AuditAnotation(
            action = Action.CREATE,
            objectType = "LESSON_FILE"
    )
    public String uploadContent(
            @PathVariable Long id,
            @Valid @ModelAttribute("uploadForm") UploadLessonFileRequest form,
            BindingResult bindingResult,
            RedirectAttributes ra
    ) {

        if (bindingResult.hasErrors()) {
            Notify.warning(ra, "Vui lòng chọn tệp bài giảng để tải lên !");
            return "redirect:/admin/lesson/" + id + "/content";
        }

        try {
            adminLessonFileService.upload(id, form.getFile());
            Notify.success(ra, "Tải lên tệp bài giảng thành công !");
        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
        }

        return "redirect:/admin/lesson/" + id + "/content";
    }

    @GetMapping("/{id}")
    @AuditAnotation(
            action = Action.VIEW,
            objectType = "LESSON_DETAIL"
    )
    public String detail(
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        try {
            Lesson lesson = adminLessonService.findById(id);
            List<LessonFile> files = adminLessonFileService.findByLesson(id);

            model.addAttribute("lesson", lesson);
            model.addAttribute("files", files);

            return "admin/lesson/detail";

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
            return "redirect:/admin/lesson/index";
        }
    }


    @PostMapping("/{id}/delete")
    @AuditAnotation(
            action = Action.DELETE,
            objectType = "LESSON"
    )
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            Lesson lesson = adminLessonService.findById(id);
            adminLessonService.delete(id);

            Notify.success(
                    ra,
                    "Đã xoá bài giảng: " + lesson.getName()
            );

        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
        }

        return "redirect:/admin/lesson/index";
    }


    @PostMapping("/content/file/{fileId}/delete")
    @AuditAnotation(
            action = Action.DELETE,
            objectType = "LESSON_CONTENT_FILE"
    )
    public String deleteLessonContent(
            @PathVariable Long fileId,
            @RequestParam Long lessonId,
            RedirectAttributes ra
    ) {
        try {
            LessonFile file = adminLessonFileService.findById(fileId);
            adminLessonFileService.deleteLessonFile(fileId);
            Notify.success(
                    ra,
                    "Đã xoá nội dung bài giảng: " + file.getFileName()
            );
        } catch (RuntimeException e) {
            Notify.error(ra, e.getMessage());
        }

        return "redirect:/admin/lesson/" + lessonId + "/content";
    }




}
