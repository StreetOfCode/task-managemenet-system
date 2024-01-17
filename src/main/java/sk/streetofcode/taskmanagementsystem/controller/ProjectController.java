package sk.streetofcode.taskmanagementsystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.streetofcode.taskmanagementsystem.api.ProjectService;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Project;

import java.util.List;

@RestController
@RequestMapping("project")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAll(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok().body(projectService.getAllByUser(userId));
        }

        return ResponseEntity.ok().body(projectService.getAll());
    }

    @PostMapping
    public ResponseEntity<Long> add(@RequestBody ProjectAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> edit(@PathVariable("id") long id, @RequestBody ProjectEditRequest request) {
        projectService.edit(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<Project> get(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(projectService.get(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        projectService.delete(id);
        return ResponseEntity.ok().build();
    }

}
