package com.bkap.teach.admin.dto.request.lession;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
public class UploadLessonFileRequest {
    @NotNull(message = "Vui lòng chọn tệp bài giảng để tải lên !")
    private MultipartFile file;

}
