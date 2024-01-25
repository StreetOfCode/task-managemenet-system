package sk.streetofcode.taskmanagementsystem.implementation.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "project")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {
    @Id
    @SequenceGenerator(name = "project_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_id_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = true)
    @Setter
    private String description;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private List<TaskEntity> tasks = new ArrayList<>();

    public ProjectEntity(UserEntity user, String name, String description, OffsetDateTime createdAt) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public ProjectEntity(long id, UserEntity user, String name, String description, OffsetDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }
}
