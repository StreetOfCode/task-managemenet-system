package sk.streetofcode.taskmanagementsystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.streetofcode.taskmanagementsystem.api.UserService;
import sk.streetofcode.taskmanagementsystem.api.request.UserAddRequest;
import sk.streetofcode.taskmanagementsystem.domain.User;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok().body(userService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<User> get(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(userService.get(id));
    }

    @PostMapping
    public ResponseEntity<Long> add(@RequestBody UserAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.add(request));
    }
}
