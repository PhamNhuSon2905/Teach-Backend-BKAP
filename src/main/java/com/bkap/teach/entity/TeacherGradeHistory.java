package com.bkap.teach.entity;

import com.bkap.teach.enums.TeacherGradeAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "teacher_grade_histories")
public class TeacherGradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(nullable = false)
    private Integer grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherGradeAction action;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @PrePersist
    public void prePersist() {
        this.actionAt = LocalDateTime.now();
    }
}
