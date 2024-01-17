package sk.streetofcode.taskmanagementsystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.streetofcode.taskmanagementsystem.api.exception.BadRequestException;
import sk.streetofcode.taskmanagementsystem.api.exception.ResourceNotFoundException;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAssignStatusRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskChangeStatusRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Task;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;

import java.util.List;

public class TaskIntegrationTests extends IntegrationTest {
    @Test
    public void getAll() {
        final ResponseEntity<List<Task>> tasks = restTemplate.exchange(
                "/task",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        Assertions.assertEquals(HttpStatus.OK, tasks.getStatusCode());
        Assertions.assertNotNull(tasks.getBody());
        Assertions.assertTrue(tasks.getBody().size() >= 2);
    }

    @Test
    public void getAllByUser() {
        final ResponseEntity<List<Task>> tasks = restTemplate.exchange(
                "/task?userId=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        Assertions.assertEquals(HttpStatus.OK, tasks.getStatusCode());
        Assertions.assertNotNull(tasks.getBody());
        Assertions.assertFalse(tasks.getBody().isEmpty());
    }

    @Test
    public void getAllByProject() {
        final ResponseEntity<List<Task>> tasks = restTemplate.exchange(
                "/task?projectId=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        Assertions.assertEquals(HttpStatus.OK, tasks.getStatusCode());
        Assertions.assertNotNull(tasks.getBody());
        Assertions.assertFalse(tasks.getBody().isEmpty());
    }

    @Test
    public void insert() {
        insertTask(generateRandomTask());
    }

    @Test
    public void insertWithoutDescription() {
        final TaskAddRequest addRequest = new TaskAddRequest(
                1L,
                1L,
                "name" + Math.random(),
                null
        );

        final ResponseEntity<Long> addTaskResponse = restTemplate.postForEntity(
                "/task",
                addRequest,
                Long.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, addTaskResponse.getStatusCode());
        final Long id = addTaskResponse.getBody();
        Assertions.assertNotNull(id);

        final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/task/" + id,
                Task.class
        );

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(id, getResponse.getBody().getId());
        Assertions.assertEquals(addRequest.getUserId(), getResponse.getBody().getUserId());
        Assertions.assertEquals(addRequest.getProjectId(), getResponse.getBody().getProjectId());
        Assertions.assertEquals(addRequest.getName(), getResponse.getBody().getName());
        Assertions.assertNull(getResponse.getBody().getDescription());
    }

    @Test
    public void insertWithoutProjectId() {
        final TaskAddRequest addRequest = new TaskAddRequest(
                1L,
                null,
                "name" + Math.random(),
                "description"
        );

        final ResponseEntity<Long> addTaskResponse = restTemplate.postForEntity(
                "/task",
                addRequest,
                Long.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, addTaskResponse.getStatusCode());
        final Long id = addTaskResponse.getBody();
        Assertions.assertNotNull(id);

        final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/task/" + id,
                Task.class
        );

        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(id, getResponse.getBody().getId());
        Assertions.assertEquals(addRequest.getUserId(), getResponse.getBody().getUserId());
        Assertions.assertNull(getResponse.getBody().getProjectId());
        Assertions.assertEquals(addRequest.getName(), getResponse.getBody().getName());
        Assertions.assertEquals(addRequest.getDescription(), getResponse.getBody().getDescription());
    }

    @Test
    public void getTask() {
        final TaskAddRequest addRequest = generateRandomTask();
        final long id = insertTask(addRequest);
        final ResponseEntity<Task> task = restTemplate.getForEntity(
                "/task/" + id,
                Task.class
        );

        Assertions.assertEquals(HttpStatus.OK, task.getStatusCode());
        Assertions.assertNotNull(task.getBody());
        Assertions.assertEquals(id, task.getBody().getId());
        Assertions.assertEquals(addRequest.getUserId(), task.getBody().getUserId());
        Assertions.assertEquals(addRequest.getName(), task.getBody().getName());
        Assertions.assertEquals(addRequest.getDescription(), task.getBody().getDescription());
        Assertions.assertEquals(TaskStatus.NEW, task.getBody().getStatus());
    }

    @Test
    public void deleteTask() {
        // create task
        final TaskAddRequest addRequest = generateRandomTask();
        final long id = insertTask(addRequest);

        // delete task
        final ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/task/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        Assertions.assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // deleted task should not exist
        final ResponseEntity<ResourceNotFoundException> getResponse = restTemplate.getForEntity(
                "/task/" + id,
                ResourceNotFoundException.class
        );

        Assertions.assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    public void update() {
        final TaskAddRequest addRequest = generateRandomTask();
        final long id = insertTask(addRequest);

        // update
        final TaskEditRequest updateRequest = new TaskEditRequest(
                "editedName",
                "editedDescription",
                TaskStatus.DONE
        );
        final ResponseEntity<Void> updateResponse = restTemplate.exchange(
                "/task/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Void.class
        );
        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // get and compare
        final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/task/" + id,
                Task.class
        );
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(id, getResponse.getBody().getId());
        Assertions.assertEquals(updateRequest.getName(), getResponse.getBody().getName());
        Assertions.assertEquals(updateRequest.getDescription(), getResponse.getBody().getDescription());
        Assertions.assertEquals(updateRequest.getStatus(), getResponse.getBody().getStatus());
    }

    @Test
    public void changeStatus() {
        final TaskAddRequest addRequest = generateRandomTask();
        final long id = insertTask(addRequest);

        // change status
        final TaskChangeStatusRequest updateRequest = new TaskChangeStatusRequest(
                TaskStatus.DONE
        );

        final ResponseEntity<Void> updateResponse = restTemplate.exchange(
                "/task/" + id + "/status",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Void.class
        );
        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // get and compare
        final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/task/" + id,
                Task.class
        );
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(id, getResponse.getBody().getId());
        Assertions.assertEquals(updateRequest.getStatus(), getResponse.getBody().getStatus());
    }

    @Test
    public void assign() {
        final TaskAddRequest addRequest = new TaskAddRequest(
                1L,
                null,
                "name",
                "description"
        );
        final long id = insertTask(addRequest);

        // assign
        final TaskAssignStatusRequest assignRequest = new TaskAssignStatusRequest(
                1L
        );
        final ResponseEntity<Void> updateResponse = restTemplate.exchange(
                "/task/" + id + "/assign",
                HttpMethod.PUT,
                new HttpEntity(assignRequest),
                Void.class
        );
        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // get and compare
        final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
                "/task/" + id,
                Task.class
        );
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals(id, getResponse.getBody().getId());
        Assertions.assertEquals(1L, getResponse.getBody().getProjectId());
    }

    @Test
    public void assignWrongUser() {
        final TaskAddRequest addRequest = new TaskAddRequest(
                1L,
                null,
                "name",
                "description"
        );
        final long id = insertTask(addRequest);

        // assign
        final TaskAssignStatusRequest assignRequest = new TaskAssignStatusRequest(
                2L
        );
        final ResponseEntity<BadRequestException> updateResponse = restTemplate.exchange(
                "/task/" + id + "/assign",
                HttpMethod.PUT,
                new HttpEntity(assignRequest),
                BadRequestException.class
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, updateResponse.getStatusCode());
    }

    private long insertTask(TaskAddRequest request) {
        final ResponseEntity<Long> task = restTemplate.postForEntity(
                "/task",
                request,
                Long.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, task.getStatusCode());
        Assertions.assertNotNull(task.getBody());
        return task.getBody();
    }

    private TaskAddRequest generateRandomTask() {
        return new TaskAddRequest(
                1L,
                1L,
                "name" + Math.random(),
                "description" + Math.random()
        );
    }
}
