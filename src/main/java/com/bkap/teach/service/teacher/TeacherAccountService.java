package com.bkap.teach.service.teacher;


import com.bkap.teach.entity.User;
import com.bkap.teach.repository.UserRepository;
import com.bkap.teach.teacher.dto.request.TeacherChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TeacherAccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void changePassword(
            Long teacherId,
            TeacherChangePasswordRequest request
    ) {

        User user = userRepository.findById(teacherId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tài khoản giảng viên!")
                );

        // Check mật khẩu hiện tại
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())
        ) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }

        // Check confirm
        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Mật khẩu mới nhập lại không khớp!");
        }

        // Không trùng mật khẩu cũ
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())
        ) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu cũ!");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );
        userRepository.save(user);
    }
}
