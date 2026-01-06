package com.bkap.teach.entity;

import com.bkap.teach.enums.Role;
import com.bkap.teach.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 150)
    private String fullname;


    @Column( name = "password", nullable = false)
    private String password;

    @Column( name = "email", nullable = false ,unique = true)
    private String email;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column( name = "phone", nullable = false , unique = false)
    private String phone;

    @Column( name = "address", nullable = false)
    private String address;

    @Column(name = "avatar" , nullable = true)
    private String avatar;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }


}
