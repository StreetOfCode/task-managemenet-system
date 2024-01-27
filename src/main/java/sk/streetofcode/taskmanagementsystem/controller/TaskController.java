package sk.streetofcode.taskmanagementsystem.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> add(@RequestBody TaskAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.add(request));
    }

    @PutMapping("{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task edited"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> edit(@PathVariable("id") long id, @RequestBody TaskEditRequest request) {
        taskService.edit(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}/status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status changed"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> changeStatus(@PathVariable("id") long id, @RequestBody TaskChangeStatusRequest request) {
        taskService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}/assign")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned"),
            @ApiResponse(responseCode = "400", description = "Task and project must belong to the same user"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> assign(@PathVariable("id") long id, @RequestBody TaskAssignStatusRequest request) {
        taskService.assign(id, request.getProjectId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Task> get(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(taskService.get(id));
    }

    @DeleteMapping("{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        taskService.delete(id);
        return ResponseEntity.ok().build();
    }
}
