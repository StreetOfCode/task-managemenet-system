package sk.streetofcode.taskmanagementsystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.streetofcode.taskmanagementsystem.api.exception.BadRequestException;
import sk.streetofcode.taskmanagementsystem.api.exception.ResourceNotFoundException;
import sk.streetofcode.taskmanagementsystem.api.request.UserAddRequest;
import sk.streetofcode.taskmanagementsystem.domain.User;

import java.util.List;

public class UserIntegrationTests extends IntegrationTest {

    @Test
    public void getAll() {
        final ResponseEntity<List<User>> users = restTemplate.exchange(
                "/user",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        Assertions.assertEquals(HttpStatus.OK, users.getStatusCode());
        Assertions.assertNotNull(users.getBody());
        Assertions.assertTrue(users.getBody().size() >= 2);
    }

    @Test
    public void insert() {
        insertUser(generateRandomUser());
    }

    @Test
    public void getUser() {
        final UserAddRequest addRequest = generateRandomUser();
        final long id = insertUser(addRequest);
        final ResponseEntity<User> user = restTemplate.getForEntity(
                "/user/" + id,
                User.class
        );

        Assertions.assertEquals(HttpStatus.OK, user.getStatusCode());
        Assertions.assertNotNull(user.getBody());
        Assertions.assertEquals(id, user.getBody().getId());
        Assertions.assertEquals(addRequest.getName(), user.getBody().getName());
        Assertions.assertEquals(addRequest.getEmail(), user.getBody().getEmail());
    }

    @Test
    public void deleteUser() {
        // create user
        final UserAddRequest addRequest = generateRandomUser();
        final long id = insertUser(addRequest);

        // delete user
        final ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/user/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        Assertions.assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // deleted user should not exist
        final ResponseEntity<ResourceNotFoundException> getResponse = restTemplate.getForEntity(
                "/user/" + id,
                ResourceNotFoundException.class
        );

        Assertions.assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    public void insertEmailAlreadyExists() {
        final UserAddRequest addRequest = generateRandomUser();
        insertUser(addRequest);

        final ResponseEntity<BadRequestException> badRequest = restTemplate.postForEntity(
                "/user",
                addRequest,
                BadRequestException.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
    }

    private long insertUser(UserAddRequest request) {
        final ResponseEntity<Long> user = restTemplate.postForEntity(
                "/user",
                request,
                Long.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, user.getStatusCode());
        Assertions.assertNotNull(user.getBody());
        return user.getBody();
    }

    private UserAddRequest generateRandomUser() {
        return new UserAddRequest(
                "test" + Math.random(),
                "email" + Math.random()
        );
    }
}
