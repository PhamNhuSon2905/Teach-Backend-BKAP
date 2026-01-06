package com.bkap.teach.service.user;

import com.bkap.teach.admin.dto.request.AdminRegisterRequest;
import com.bkap.teach.teacher.dto.request.TeacherRegisterRequest;
import com.bkap.teach.entity.User;
import com.bkap.teach.repository.UserRepository;
import com.bkap.teach.enums.Role;
import com.bkap.teach.enums.Action;
import com.bkap.teach.enums.Status;
import com.bkap.teach.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    public Page<User> searchUsers(
            int pageNo,
            int pageSize,
            String keyword,
            Role role
    ) {

        Pageable pageable = PageRequest.of(
                pageNo - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "id")
        );

        String kw = (keyword == null) ? "" : keyword.trim();
        if (role == null && kw.isEmpty()) {
            return userRepository.findAll(pageable);
        }
        if (role == null) {
            return userRepository
                    .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            kw,
                            kw,
                            pageable
                    );
        }
        return userRepository
                .findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        role,
                        kw,
                        role,
                        kw,
                        pageable
                );
    }

    public List<User> findAllTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }


    public void changeStatusWithRule(
            Long targetUserId,
            Status status,
            Long currentUserId
    ) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));


        if (targetUser.getId().equals(currentUserId)) {
            throw new RuntimeException("Không thể tự khoá tài khoản của mình");
        }


        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể thay đổi trạng thái tài khoản ADMIN");
        }


        if (targetUser.getRole() != Role.TEACHER) {
            throw new RuntimeException("Chỉ được thao tác với tài khoản giảng viên");
        }


        targetUser.setStatus(status);
        userRepository.save(targetUser);
    }

    public void deleteWithRule(
            Long targetUserId,
            Long currentUserId
    ) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hiện tại !"));


        if (targetUser.getId().equals(currentUserId)) {
            throw new RuntimeException("Không thể xoá chính tài khoản của mình !");
        }


        if (targetUser.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể xoá tài khoản quản trị !");
        }


        if (targetUser.getRole() != Role.TEACHER) {
            throw new RuntimeException("Chỉ được xoá tài khoản giảng viên !");
        }


        if (targetUser.getStatus() != Status.LOCKED) {
            throw new RuntimeException("Chỉ được xoá tài khoản đã bị khoá !");
        }


        userRepository.delete(targetUser);
    }





    public void registerAdmin(
            AdminRegisterRequest request,
            HttpServletRequest httpRequest
    ) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên người dùng đã tồn tại !");
        }

        if (request.getEmail() != null
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng !");
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullname(request.getFullname())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(user);

        auditLogService.logSimple(
                Action.REGISTER,
                "ADMIN_REGISTER",
                user.getId(),
                user.getUsername(),
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                httpRequest
        );
    }

    public void createTeacher(
            TeacherRegisterRequest request,
            Long adminId
    ) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên người dùng đã tồn tại !");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullname(request.getFullname())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.TEACHER)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(user);
    }

    public User getUserDetail(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hiện tại !"));
    }

    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập tài khoản !");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() ->
                            new RuntimeException("Không tìm thấy người dùng đang đăng nhập !"));
        }

        throw new RuntimeException("Không xác định được người dùng hiện tại !");
    }


}
