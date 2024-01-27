package sk.streetofcode.taskmanagementsystem.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> get(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(userService.get(id));
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Bad request, email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> add(@RequestBody UserAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.add(request));
    }

    @DeleteMapping("{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
