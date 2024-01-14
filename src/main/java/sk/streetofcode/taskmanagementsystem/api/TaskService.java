package sk.streetofcode.taskmanagementsystem.api;

import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Task;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;

import java.util.List;

public interface TaskService {
    long add(TaskAddRequest request);
    void edit(long taskId, TaskEditRequest request);
    void changeStatus(long taskId, TaskStatus status);
    void assign(long taskId, long projectId);
    void delete(long taskId);
    Task get(long taskId);
    List<Task> getAll();
    List<Task> getAllByUser(long userId);
    List<Task> getAllByProject(long projectId);
}
