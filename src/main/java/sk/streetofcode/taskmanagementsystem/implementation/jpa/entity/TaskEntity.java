package sk.streetofcode.taskmanagementsystem.implementation.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;

import java.time.OffsetDateTime;

@Entity(name = "task")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {
    @Id
    @SequenceGenerator(name = "task_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_id_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    @Setter
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", nullable = true)
    @Setter
    private UserEntity assignedUser;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = true)
    @Setter
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private TaskStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public TaskEntity(UserEntity user, ProjectEntity project, UserEntity assignedUser, String name, String description, TaskStatus status, OffsetDateTime createdAt) {
        this.user = user;
        this.project = project;
        this.assignedUser = assignedUser;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }
}
