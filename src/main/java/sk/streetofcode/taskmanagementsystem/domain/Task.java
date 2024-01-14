package sk.streetofcode.taskmanagementsystem.domain;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class Task {
    long id;
    long userId;
    Long projectId;
    String name;
    String description;
    TaskStatus status;
    OffsetDateTime createdAt;
}
