package sk.streetofcode.taskmanagementsystem.implementation.jpa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.ProjectService;
import sk.streetofcode.taskmanagementsystem.api.TaskService;
import sk.streetofcode.taskmanagementsystem.api.UserService;
import sk.streetofcode.taskmanagementsystem.api.exception.BadRequestException;
import sk.streetofcode.taskmanagementsystem.api.exception.InternalErrorException;
import sk.streetofcode.taskmanagementsystem.api.exception.ResourceNotFoundException;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Project;
import sk.streetofcode.taskmanagementsystem.domain.Task;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;
import sk.streetofcode.taskmanagementsystem.domain.User;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.entity.ProjectEntity;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.entity.TaskEntity;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.entity.UserEntity;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.repository.TaskJpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Primary
public class TaskServiceJpaImpl implements TaskService {

    private final TaskJpaRepository repository;
    private final UserService userService;
    private final ProjectService projectService;
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceJpaImpl.class);

    public TaskServiceJpaImpl(TaskJpaRepository repository, UserService userService, ProjectService projectService) {
        this.repository = repository;
        this.userService = userService;
        this.projectService = projectService;
    }


    @Override
    public long add(TaskAddRequest request) {
        final User user = userService.get(request.getUserId());
        final UserEntity userEntity = new UserEntity(user.getId(), user.getName(), user.getEmail());

        final ProjectEntity projectEntity;
        if (request.getProjectId() == null) {
            projectEntity = null;
        } else {
            final Project project = projectService.get(request.getProjectId());
            projectEntity = new ProjectEntity(project.getId(), userEntity, project.getName(), project.getDescription(), OffsetDateTime.now());
        }

        try {
            return repository.save(new TaskEntity(userEntity, projectEntity, request.getName(), request.getDescription(), TaskStatus.NEW, OffsetDateTime.now())).getId();
        } catch (DataAccessException e) {
            logger.error("Error while adding task", e);
            throw new InternalErrorException("Error while adding task");
        }
    }

    @Override
    public void edit(long taskId, TaskEditRequest request) {
        final TaskEntity taskEntity = repository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task with id " + taskId + " not found"));
        taskEntity.setName(request.getName());
        taskEntity.setDescription(request.getDescription());
        taskEntity.setStatus(request.getStatus());
        repository.save(taskEntity);
    }

    @Override
    public void changeStatus(long taskId, TaskStatus status) {
        final TaskEntity taskEntity = repository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task with id " + taskId + " not found"));
        taskEntity.setStatus(status);
        repository.save(taskEntity);
    }

    @Override
    public void assign(long taskId, long projectId) {
        final TaskEntity taskEntity = repository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task with id " + taskId + " not found"));
        final Project project = projectService.get(projectId);

        if (!Objects.equals(project.getUserId(), taskEntity.getUser().getId())) {
            throw new BadRequestException("Task and project must belong to the same user");
        }

        final ProjectEntity projectEntity = new ProjectEntity(project.getId(), taskEntity.getUser(), project.getName(), project.getDescription(), OffsetDateTime.now());

        taskEntity.setProject(projectEntity);
        repository.save(taskEntity);
    }

    @Override
    public void delete(long taskId) {
        if (this.get(taskId) != null) {
            repository.deleteById(taskId);
        }
    }

    @Override
    public Task get(long taskId) {
        return repository
                .findById(taskId).map(this::mapTaskEntityToTask)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + taskId + " not found"));
    }

    @Override
    public List<Task> getAll() {
        return repository.findAll().stream().map(this::mapTaskEntityToTask).toList();
    }

    @Override
    public List<Task> getAllByUser(long userId) {
        if (userService.get(userId) != null) {
            return repository.findAllByUserId(userId).stream().map(this::mapTaskEntityToTask).toList();
        }

        return null;
    }

    @Override
    public List<Task> getAllByProject(long projectId) {
        if (projectService.get(projectId) != null) {
            return repository.findAllByProjectId(projectId).stream().map(this::mapTaskEntityToTask).toList();
        }

        return null;
    }

    private Task mapTaskEntityToTask(TaskEntity taskEntity) {
        return new Task(
                taskEntity.getId(),
                taskEntity.getUser().getId(),
                taskEntity.getProject() != null ? taskEntity.getProject().getId() : null,
                taskEntity.getName(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getCreatedAt()
        );
    }
}
