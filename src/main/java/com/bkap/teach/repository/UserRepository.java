package com.bkap.teach.repository;

import java.util.Optional;
import java.util.List;
import com.bkap.teach.enums.Role;
import com.bkap.teach.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bkap.teach.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    long countByRole(Role role);
    long countByStatus(Status status);
    Page<User> findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role role1,
            String username,
            Role role2,
            String email,
            Pageable pageable
    );
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username,
            String email,
            Pageable pageable
    );


}
