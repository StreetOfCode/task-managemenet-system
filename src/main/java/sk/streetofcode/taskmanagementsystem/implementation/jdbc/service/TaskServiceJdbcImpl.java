package sk.streetofcode.taskmanagementsystem.implementation.jdbc.service;

import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.TaskService;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Task;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;

import java.util.List;

@Service
public class TaskServiceJdbcImpl implements TaskService {
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
        return null;
    }

    @Override
    public List<Task> getAll() {
        return null;
    }

    @Override
    public List<Task> getAllByUser(long userId) {
        return null;
    }

    @Override
    public List<Task> getAllByProject(long projectId) {
        return null;
    }
}
