package sk.streetofcode.taskmanagementsystem.domain;

import java.time.OffsetDateTime;

public class Task {
    private final long id;
    private final long userId;
    private final Long projectId;
    private final String name;
    private final String description;
    private final TaskStatus status;
    private final OffsetDateTime createdAt;

    public Task(long id, long userId, Long projectId, String name, String description, TaskStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
