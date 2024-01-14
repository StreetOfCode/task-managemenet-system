package sk.streetofcode.taskmanagementsystem.domain;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class Project {
    long id;
    long userId;
    String name;
    String description;
    OffsetDateTime createdAt;
}
