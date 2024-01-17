package sk.streetofcode.taskmanagementsystem.implementation.jdbc.service;

import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.ProjectService;
import sk.streetofcode.taskmanagementsystem.api.TaskService;
import sk.streetofcode.taskmanagementsystem.api.UserService;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Task;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository.TaskJdbcRepository;

import java.util.List;

@Service
public class TaskServiceJdbcImpl implements TaskService {
    private final TaskJdbcRepository repository;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskServiceJdbcImpl(TaskJdbcRepository repository, ProjectService projectService, UserService userService) {
        this.repository = repository;
        this.projectService = projectService;
        this.userService = userService;
    }

    @Override
    public long add(TaskAddRequest request) {
        return 0;
    }

    @Override
    public void edit(long taskId, TaskEditRequest request) {

    }

    @Override
    public void changeStatus(long taskId, TaskStatus status) {

    }

    @Override
    public void assign(long taskId, long projectId) {

    }

    @Override
    public void delete(long taskId) {

    }

    @Override
    public Task get(long taskId) {
        return repository.getById(taskId);
    }

    @Override
    public List<Task> getAll() {
        return repository.getAll();
    }

    @Override
    public List<Task> getAllByUser(long userId) {
        if (userService.get(userId) != null) {
            return repository.getAllByUser(userId);
        }

        return null;
    }

    @Override
    public List<Task> getAllByProject(long projectId) {
        if (projectService.get(projectId) != null) {
            return repository.getAllByProject(projectId);
        }

        return null;
    }
}
