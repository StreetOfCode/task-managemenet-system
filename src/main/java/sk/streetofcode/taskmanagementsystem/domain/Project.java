package sk.streetofcode.taskmanagementsystem.domain;

public class Project {
    private final long id;
    private final long userId;
    private final String name;
    private final String description;

    public Project(long id, long userId, String name, String description) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
