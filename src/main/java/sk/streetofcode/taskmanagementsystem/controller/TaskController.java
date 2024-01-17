package sk.streetofcode.taskmanagementsystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.streetofcode.taskmanagementsystem.api.TaskService;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAssignStatusRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskChangeStatusRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Task;

import java.util.List;

@RestController
@RequestMapping("task")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId
    ) {
        if (userId != null) {
            return ResponseEntity.ok().body(taskService.getAllByUser(userId));
        } else if (projectId != null) {
            return ResponseEntity.ok().body(taskService.getAllByProject(projectId));
        }

        return ResponseEntity.ok().body(taskService.getAll());
    }

    @PostMapping
    public ResponseEntity<Long> add(@RequestBody TaskAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> edit(@PathVariable("id") long id, @RequestBody TaskEditRequest request) {
        taskService.edit(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable("id") long id, @RequestBody TaskChangeStatusRequest request) {
        taskService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}/assign")
    public ResponseEntity<Void> assign(@PathVariable("id") long id, @RequestBody TaskAssignStatusRequest request) {
        taskService.assign(id, request.getProjectId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<Task> get(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(taskService.get(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        taskService.delete(id);
        return ResponseEntity.ok().build();
    }
}
