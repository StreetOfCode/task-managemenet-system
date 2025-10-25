# PRP: Add Assigned User to Tasks

## Feature Summary

Add a new optional field `assignedUserId` to tasks that represents **who should complete the task**. This is different from the existing `userId` field which represents who **created** the task.

**Key Requirements:**
- The field is **nullable** - tasks can exist without an assigned user
- The assigned user can be **changed** (null → user, user → different user, user → null)
- Must work in **BOTH** JPA and JDBC profiles
- Must validate that the assigned user exists in the database
- No cross-user validation needed (unlike project assignment where task and project must belong to same user)

## Context from Codebase

### Existing Similar Pattern: `projectId`

The codebase already has a perfect example of a nullable foreign key relationship with `projectId`. Follow this exact pattern for `assignedUserId`.

**Reference Files:**
- `src/main/java/sk/streetofcode/taskmanagementsystem/domain/Task.java:11` - projectId field (Long, nullable)
- `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jpa/entity/TaskEntity.java:26-29` - nullable @ManyToOne project relationship
- `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/mapper/TaskRowMapper.java:17` - nullable projectId mapping
- `src/main/resources/schema.sql:24` - project_id column (nullable with FK constraint)

### JPA Pattern

**TaskEntity.java already shows the pattern:**
```java
// Line 22-24: Non-nullable user (creator)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private UserEntity user;

// Line 26-29: Nullable project (use this pattern for assignedUser)
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "project_id", nullable = true)
@Setter
private ProjectEntity project;
```

**Key JPA Details:**
- `@ManyToOne(fetch = FetchType.LAZY)` - lazy loading for performance
- `@JoinColumn(name = "assigned_user_id", nullable = true)` - explicit nullable for schema generation
- `@Setter` annotation - allows changing the assigned user after creation
- No `@Setter` on `user` field - creator cannot be changed

### JDBC Pattern

**TaskRowMapper.java shows the pattern (line 17):**
```java
rs.getObject("project_id") != null ? rs.getLong("project_id"): null
```

**TaskJdbcRepository.java shows PreparedStatement pattern (lines 69-73):**
```java
if (request.getProjectId() != null) {
    ps.setLong(2, request.getProjectId());
} else {
    ps.setNull(2, java.sql.Types.BIGINT);  // CRITICAL: Use java.sql.Types.BIGINT for nullable FK
}
```

### Service Layer Pattern

**TaskServiceJpaImpl.java shows entity creation pattern (lines 47-56):**
```java
final User user = userService.get(request.getUserId());  // Validate user exists
final UserEntity userEntity = new UserEntity(user.getId(), user.getName(), user.getEmail());

final ProjectEntity projectEntity;
if (request.getProjectId() == null) {
    projectEntity = null;
} else {
    final Project project = projectService.get(request.getProjectId());
    projectEntity = new ProjectEntity(project.getId(), userEntity, project.getName(), project.getDescription(), OffsetDateTime.now());
}
```

**Follow this exact pattern for assignedUser.**

## External Documentation

### Spring Data JPA
- **ManyToOne nullable relationships**: https://stackoverflow.com/questions/25718229/can-a-manytoone-jpa-relation-be-null
  - By default, @ManyToOne is nullable (nullable=true is default)
  - Use `@JoinColumn(nullable=true)` for explicit schema generation
  - Use `@Setter` to allow updates to the relationship

### JDBC PreparedStatement
- **Handling nullable foreign keys**: https://www.baeldung.com/jdbc-insert-null-into-integer-column
  - Must use `ps.setNull(index, java.sql.Types.BIGINT)` for nullable foreign keys
  - Cannot use `ps.setLong()` with null value - will throw NullPointerException
- **Spring JdbcTemplate with nulls**: https://stackoverflow.com/questions/52820230/how-to-insert-null-value-to-foreign-key-with-jdbctemplate

## Implementation Blueprint

### Phase 1: Database Schema

#### File: `src/main/resources/schema.sql`

**Location:** After line 27 (after `status` column), add new column:

```sql
assigned_user_id bigint,
```

**Location:** After line 31 (after last constraint), add new constraint:

```sql
CONSTRAINT task_assigned_user_id_fk FOREIGN KEY (assigned_user_id) REFERENCES users (id)
```

**Complete task table should look like:**
```sql
DROP TABLE IF EXISTS task;
CREATE TABLE task (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  project_id bigint,
  name varchar(45) NOT NULL,
  description varchar(160),
  status varchar(10) NOT NULL,
  created_at datetime NOT NULL,
  assigned_user_id bigint,
  PRIMARY KEY (id),
  CONSTRAINT task_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT task_project_id_fk FOREIGN KEY (project_id) REFERENCES project (id),
  CONSTRAINT task_assigned_user_id_fk FOREIGN KEY (assigned_user_id) REFERENCES users (id)
);
```

#### File: `src/main/resources/data.sql`

**Update INSERT statements (line 14-18):**

Current format:
```sql
INSERT INTO task VALUES
(next value for task_id_seq, user_id, project_id, name, description, status, created_at)
```

New format (add assigned_user_id as last column before created_at):
```sql
INSERT INTO task VALUES
(next value for task_id_seq, user_id, project_id, name, description, status, created_at, assigned_user_id)
```

**Example test data:**
```sql
INSERT INTO task VALUES
(next value for task_id_seq, 1, 1, 'Spravit API', 'API ma byt pre noveho klienta', 'DONE', CURRENT_TIMESTAMP, 1),
(next value for task_id_seq, 1, 1, 'Otestovat API', 'Unit testy + integracne testy', 'NEW', CURRENT_TIMESTAMP, 2),
(next value for task_id_seq, 2, 2, 'Kupit mame darcek', null, 'NEW', CURRENT_TIMESTAMP, null),
(next value for task_id_seq, 2, null, 'Zavolat do skoly', 'Cislo mam na vizitke riaditelky', 'NEW', CURRENT_TIMESTAMP, 2);
```

### Phase 2: Domain Model

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/domain/Task.java`

**Add field after line 11 (after projectId):**

```java
Long assignedUserId;
```

**Complete Task class should have these fields:**
```java
@Value
public class Task {
    long id;
    long userId;
    Long projectId;
    Long assignedUserId;  // NEW
    String name;
    String description;
    TaskStatus status;
    OffsetDateTime createdAt;
}
```

### Phase 3: JPA Implementation

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jpa/entity/TaskEntity.java`

**Add field after line 29 (after project field):**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "assigned_user_id", nullable = true)
@Setter
private UserEntity assignedUser;
```

**Update constructor (line 47) - add assignedUser parameter:**

Current:
```java
public TaskEntity(UserEntity user, ProjectEntity project, String name, String description, TaskStatus status, OffsetDateTime createdAt)
```

New:
```java
public TaskEntity(UserEntity user, ProjectEntity project, UserEntity assignedUser, String name, String description, TaskStatus status, OffsetDateTime createdAt)
```

**Update constructor body (line 48-54) - add assignment:**
```java
this.user = user;
this.project = project;
this.assignedUser = assignedUser;  // NEW
this.name = name;
this.description = description;
this.status = status;
this.createdAt = createdAt;
```

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jpa/service/TaskServiceJpaImpl.java`

**Update add() method (lines 46-59):**

After handling projectEntity (around line 56), add assignedUser handling:

```java
final UserEntity assignedUserEntity;
if (request.getAssignedUserId() == null) {
    assignedUserEntity = null;
} else {
    final User assignedUser = userService.get(request.getAssignedUserId());
    assignedUserEntity = new UserEntity(assignedUser.getId(), assignedUser.getName(), assignedUser.getEmail());
}
```

**Update repository.save() call (line 59):**

Current:
```java
return repository.save(new TaskEntity(userEntity, projectEntity, request.getName(), request.getDescription(), TaskStatus.NEW, OffsetDateTime.now())).getId();
```

New:
```java
return repository.save(new TaskEntity(userEntity, projectEntity, assignedUserEntity, request.getName(), request.getDescription(), TaskStatus.NEW, OffsetDateTime.now())).getId();
```

**Update edit() method (lines 67-73):**

After line 71 (after setStatus), add:
```java
// Handle assignedUser update
if (request.getAssignedUserId() == null) {
    taskEntity.setAssignedUser(null);
} else {
    final User assignedUser = userService.get(request.getAssignedUserId());
    taskEntity.setAssignedUser(new UserEntity(assignedUser.getId(), assignedUser.getName(), assignedUser.getEmail()));
}
```

**Add new assignUser() method (after changeStatus method, around line 81):**

```java
@Override
public void assignUser(long taskId, Long assignedUserId) {
    final TaskEntity taskEntity = repository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task with id " + taskId + " not found"));

    if (assignedUserId == null) {
        taskEntity.setAssignedUser(null);
    } else {
        final User assignedUser = userService.get(assignedUserId);
        taskEntity.setAssignedUser(new UserEntity(assignedUser.getId(), assignedUser.getName(), assignedUser.getEmail()));
    }

    repository.save(taskEntity);
}
```

**Update mapTaskEntityToTask() method (lines 134-144):**

After line 138 (after project mapping), add:
```java
taskEntity.getAssignedUser() != null ? taskEntity.getAssignedUser().getId() : null,
```

**Complete method should look like:**
```java
private Task mapTaskEntityToTask(TaskEntity taskEntity) {
    return new Task(
            taskEntity.getId(),
            taskEntity.getUser().getId(),
            taskEntity.getProject() != null ? taskEntity.getProject().getId() : null,
            taskEntity.getAssignedUser() != null ? taskEntity.getAssignedUser().getId() : null,  // NEW
            taskEntity.getName(),
            taskEntity.getDescription(),
            taskEntity.getStatus(),
            taskEntity.getCreatedAt()
    );
}
```

### Phase 4: JDBC Implementation

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/mapper/TaskRowMapper.java`

**Update mapRow() method (lines 14-22):**

After line 17 (after projectId mapping), add:
```java
rs.getObject("assigned_user_id") != null ? rs.getLong("assigned_user_id") : null,
```

**Complete method should look like:**
```java
@Override
public Task mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
    return new Task(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getObject("project_id") != null ? rs.getLong("project_id"): null,
            rs.getObject("assigned_user_id") != null ? rs.getLong("assigned_user_id") : null,  // NEW
            rs.getString("name"),
            rs.getString("description"),
            TaskStatus.fromString(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime().atOffset(ZoneOffset.UTC)
    );
}
```

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/repository/TaskJdbcRepository.java`

**Update INSERT query (line 45):**

Current:
```java
INSERT = "INSERT INTO task(id, user_id, project_id, name, description, status, created_at) VALUES (next value for task_id_seq, ?, ?, ?, ?, ?, ?)";
```

New:
```java
INSERT = "INSERT INTO task(id, user_id, project_id, name, description, status, created_at, assigned_user_id) VALUES (next value for task_id_seq, ?, ?, ?, ?, ?, ?, ?)";
```

**Update UPDATE query (line 46):**

Current:
```java
UPDATE = "UPDATE task SET name = ?, description = ?, status = ? WHERE id = ?";
```

New:
```java
UPDATE = "UPDATE task SET name = ?, description = ?, status = ?, assigned_user_id = ? WHERE id = ?";
```

**Add new UPDATE_ASSIGNED_USER query (after line 48):**

```java
private static final String UPDATE_ASSIGNED_USER = "UPDATE task SET assigned_user_id = ? WHERE id = ?";
```

**Update static initialization block (line 48):**
```java
UPDATE_ASSIGNED_USER = "UPDATE task SET assigned_user_id = ? WHERE id = ?";
```

**Update add() method (lines 63-95):**

After line 73 (after project_id handling), add assigned_user_id handling:

```java
ps.setString(3, request.getName());
if (request.getDescription() != null) {
    ps.setString(4, request.getDescription());
} else {
    ps.setNull(4, java.sql.Types.VARCHAR);
}
ps.setString(5, TaskStatus.NEW.toString());
ps.setTimestamp(6, Timestamp.from(OffsetDateTime.now().toInstant()));
// NEW: Handle assigned_user_id
if (request.getAssignedUserId() != null) {
    ps.setLong(7, request.getAssignedUserId());
} else {
    ps.setNull(7, java.sql.Types.BIGINT);
}
```

**Update update() method (lines 97-104):**

Current:
```java
jdbcTemplate.update(UPDATE, request.getName(), request.getDescription(), request.getStatus().toString(), id);
```

New:
```java
jdbcTemplate.update(UPDATE, request.getName(), request.getDescription(), request.getStatus().toString(),
    request.getAssignedUserId(), id);
```

**Add new updateAssignedUser() method (after updateProject method, around line 123):**

```java
public void updateAssignedUser(long id, Long assignedUserId) {
    try {
        if (assignedUserId != null) {
            jdbcTemplate.update(UPDATE_ASSIGNED_USER, assignedUserId, id);
        } else {
            jdbcTemplate.update(UPDATE_ASSIGNED_USER, new Object[]{null, id}, new int[]{java.sql.Types.BIGINT, java.sql.Types.BIGINT});
        }
    } catch (DataAccessException e) {
        logger.error("Error while updating task assigned user", e);
        throw new InternalErrorException("Error while updating task assigned user");
    }
}
```

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/implementation/jdbc/service/TaskServiceJdbcImpl.java`

**Update edit() method (lines 37-41):**

No changes needed - the repository.update() call will handle it.

**Add new assignUser() method (after assign method, around line 62):**

```java
@Override
public void assignUser(long taskId, Long assignedUserId) {
    final Task task = this.get(taskId);

    if (task != null) {
        if (assignedUserId != null) {
            // Validate that assigned user exists
            userService.get(assignedUserId);
        }
        repository.updateAssignedUser(taskId, assignedUserId);
    }
}
```

### Phase 5: API Layer

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/api/request/TaskAddRequest.java`

**Add field after line 12 (after projectId):**

```java
private Long assignedUserId;
```

**Complete class:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAddRequest {
    private Long userId;
    private Long projectId;
    private Long assignedUserId;  // NEW
    private String name;
    private String description;
}
```

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/api/request/TaskEditRequest.java`

**Add field after line 13 (after description):**

```java
private Long assignedUserId;
```

**Complete class:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEditRequest {
    private String name;
    private String description;
    private TaskStatus status;
    private Long assignedUserId;  // NEW
}
```

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/api/TaskService.java`

**Add method after line 14 (after assign method):**

```java
void assignUser(long taskId, Long assignedUserId);
```

**Complete interface:**
```java
public interface TaskService {
    long add(TaskAddRequest request);
    void edit(long taskId, TaskEditRequest request);
    void changeStatus(long taskId, TaskStatus status);
    void assign(long taskId, long projectId);
    void assignUser(long taskId, Long assignedUserId);  // NEW
    void delete(long taskId);
    Task get(long taskId);
    List<Task> getAll();
    List<Task> getAllByUser(long userId);
    List<Task> getAllByProject(long projectId);
}
```

#### File: `src/main/java/sk/streetofcode/taskmanagementsystem/controller/TaskController.java`

**Create new request DTO class first:**

Create new file: `src/main/java/sk/streetofcode/taskmanagementsystem/api/request/TaskAssignUserRequest.java`

```java
package sk.streetofcode.taskmanagementsystem.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignUserRequest {
    private Long assignedUserId;
}
```

**Add endpoint after line 91 (after assign endpoint):**

```java
@PutMapping("{id}/assign-user")
@Operation(summary = "Assign a task to a user")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task assigned to user"),
        @ApiResponse(responseCode = "404", description = "Task or user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public ResponseEntity<Void> assignUser(@PathVariable("id") long id, @RequestBody TaskAssignUserRequest request) {
    taskService.assignUser(id, request.getAssignedUserId());
    return ResponseEntity.ok().build();
}
```

### Phase 6: Tests

#### File: `src/test/java/sk/streetofcode/taskmanagementsystem/TaskIntegrationTests.java`

**Update generateRandomTask() method (line 309):**

Current:
```java
return new TaskAddRequest(
        1L,
        1L,
        "name" + Math.random(),
        "description" + Math.random()
);
```

New (add assignedUserId):
```java
return new TaskAddRequest(
        1L,
        1L,
        null,  // assignedUserId - null by default
        "name" + Math.random(),
        "description" + Math.random()
);
```

**Add new test methods at the end of the class:**

```java
@Test
public void insertWithAssignedUser() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            1L,
            2L,  // assignedUserId
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
    Assertions.assertEquals(2L, getResponse.getBody().getAssignedUserId());
}

@Test
public void insertWithoutAssignedUser() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            1L,
            null,  // assignedUserId - null
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
    Assertions.assertNull(getResponse.getBody().getAssignedUserId());
}

@Test
public void assignUserToTask() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            null,
            null,  // No assigned user initially
            "name",
            "description"
    );
    final long id = insertTask(addRequest);

    // Assign user
    final TaskAssignUserRequest assignRequest = new TaskAssignUserRequest(2L);
    final ResponseEntity<Void> assignResponse = restTemplate.exchange(
            "/task/" + id + "/assign-user",
            HttpMethod.PUT,
            new HttpEntity<>(assignRequest),
            Void.class
    );
    Assertions.assertEquals(HttpStatus.OK, assignResponse.getStatusCode());

    // Verify assignment
    final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
            "/task/" + id,
            Task.class
    );
    Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    Assertions.assertNotNull(getResponse.getBody());
    Assertions.assertEquals(2L, getResponse.getBody().getAssignedUserId());
}

@Test
public void changeAssignedUser() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            null,
            1L,  // Initially assigned to user 1
            "name",
            "description"
    );
    final long id = insertTask(addRequest);

    // Change assigned user to user 2
    final TaskAssignUserRequest assignRequest = new TaskAssignUserRequest(2L);
    final ResponseEntity<Void> assignResponse = restTemplate.exchange(
            "/task/" + id + "/assign-user",
            HttpMethod.PUT,
            new HttpEntity<>(assignRequest),
            Void.class
    );
    Assertions.assertEquals(HttpStatus.OK, assignResponse.getStatusCode());

    // Verify new assignment
    final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
            "/task/" + id,
            Task.class
    );
    Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    Assertions.assertNotNull(getResponse.getBody());
    Assertions.assertEquals(2L, getResponse.getBody().getAssignedUserId());
}

@Test
public void unassignUser() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            null,
            1L,  // Initially assigned to user 1
            "name",
            "description"
    );
    final long id = insertTask(addRequest);

    // Unassign user (set to null)
    final TaskAssignUserRequest assignRequest = new TaskAssignUserRequest(null);
    final ResponseEntity<Void> assignResponse = restTemplate.exchange(
            "/task/" + id + "/assign-user",
            HttpMethod.PUT,
            new HttpEntity<>(assignRequest),
            Void.class
    );
    Assertions.assertEquals(HttpStatus.OK, assignResponse.getStatusCode());

    // Verify unassignment
    final ResponseEntity<Task> getResponse = restTemplate.getForEntity(
            "/task/" + id,
            Task.class
    );
    Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    Assertions.assertNotNull(getResponse.getBody());
    Assertions.assertNull(getResponse.getBody().getAssignedUserId());
}

@Test
public void assignNonExistentUser() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            null,
            null,
            "name",
            "description"
    );
    final long id = insertTask(addRequest);

    // Try to assign non-existent user
    final TaskAssignUserRequest assignRequest = new TaskAssignUserRequest(999L);
    final ResponseEntity<ResourceNotFoundException> assignResponse = restTemplate.exchange(
            "/task/" + id + "/assign-user",
            HttpMethod.PUT,
            new HttpEntity<>(assignRequest),
            ResourceNotFoundException.class
    );
    Assertions.assertEquals(HttpStatus.NOT_FOUND, assignResponse.getStatusCode());
}

@Test
public void updateTaskWithAssignedUser() {
    final TaskAddRequest addRequest = new TaskAddRequest(
            1L,
            1L,
            null,  // No assigned user initially
            "name" + Math.random(),
            "description" + Math.random()
    );
    final long id = insertTask(addRequest);

    // Update task and assign user
    final TaskEditRequest updateRequest = new TaskEditRequest(
            "editedName",
            "editedDescription",
            TaskStatus.DONE,
            2L  // Assign to user 2
    );
    final ResponseEntity<Void> updateResponse = restTemplate.exchange(
            "/task/" + id,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            Void.class
    );
    Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    // Verify update and assignment
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
    Assertions.assertEquals(2L, getResponse.getBody().getAssignedUserId());
}
```

## Critical Implementation Notes

### Gotchas and Edge Cases

1. **JDBC Null Handling**
   - MUST use `ps.setNull(index, java.sql.Types.BIGINT)` for null foreign keys
   - CANNOT use `ps.setLong()` with null - throws NullPointerException
   - Use `rs.getObject("assigned_user_id") != null` check before `rs.getLong()`

2. **JPA Lazy Loading**
   - Use `FetchType.LAZY` for all @ManyToOne relationships (performance)
   - Entities must be detached before converting to domain objects
   - Use `@Setter` on assignedUser to allow updates

3. **Constructor Order**
   - When adding new fields to constructors, update ALL usages
   - TaskEntity constructor needs assignedUser parameter
   - Task domain constructor needs assignedUserId parameter

4. **Validation**
   - Use `userService.get(assignedUserId)` to validate user exists
   - This throws ResourceNotFoundException if user not found (correct behavior)
   - No cross-user validation needed (unlike project assignment)

5. **Test Data**
   - Update data.sql INSERT statements to include assigned_user_id
   - Some tasks should have null (unassigned), some should have values
   - Ensure test users (1 and 2) exist in data.sql

6. **TaskEditRequest Update**
   - Since TaskEditRequest gets a new field, update() method signature changes
   - Both JPA and JDBC implementations must handle the new field
   - In JDBC, the UPDATE SQL needs the new parameter

### Validation Strategy

**User Existence Validation:**
```java
// This is how to validate user exists:
final User assignedUser = userService.get(assignedUserId);  // Throws ResourceNotFoundException if not found
```

**No Cross-User Validation:**
- Unlike project assignment (where task and project must belong to same user)
- Assigned user can be ANY valid user
- User who creates task (userId) can be different from assigned user (assignedUserId)

### Testing Strategy

**Test Coverage Required:**
1. ✅ Create task WITH assigned user
2. ✅ Create task WITHOUT assigned user (null)
3. ✅ Assign user to unassigned task (null → user)
4. ✅ Change assigned user (user → different user)
5. ✅ Unassign user (user → null)
6. ✅ Try to assign non-existent user (should fail with 404)
7. ✅ Update task and change assigned user in same request
8. ✅ All existing tests still pass (updated to use new constructor)

## Validation Gates

### Step 1: Clean Build
```bash
./mvnw clean install
```
**Expected:** Build succeeds, no compilation errors.

### Step 2: Test with JDBC Profile
```bash
./mvnw clean test -Dspring.profiles.active=jdbc
```
**Expected:** All tests pass, including new assigned user tests.

### Step 3: Test with JPA Profile
```bash
./mvnw clean test -Dspring.profiles.active=jpa
```
**Expected:** All tests pass, including new assigned user tests.

### Step 4: Run Application (JPA)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa
```
**Manual verification in Swagger UI (http://localhost:8080/swagger-ui/index.html):**
1. Create task with assignedUserId
2. Create task without assignedUserId
3. Use PUT /task/{id}/assign-user to assign/unassign users
4. Verify GET /task/{id} returns correct assignedUserId

### Step 5: Run Application (JDBC)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc
```
**Repeat same manual verification as Step 4.**

### Step 6: H2 Console Verification
**Access:** http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)

**Verify schema:**
```sql
SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'TASK';
```
Should show `assigned_user_id` column.

**Verify data:**
```sql
SELECT id, user_id, assigned_user_id, name FROM task;
```
Should show tasks with various assigned_user_id values (some null, some not).

## Implementation Checklist

- [ ] Update schema.sql (add column + FK constraint)
- [ ] Update data.sql (add assigned_user_id to INSERTs)
- [ ] Update Task.java (add assignedUserId field)
- [ ] Update TaskEntity.java (add assignedUser field + update constructor)
- [ ] Update TaskServiceJpaImpl.java (handle assignedUser in add/edit/assignUser + update mapper)
- [ ] Update TaskRowMapper.java (map assigned_user_id)
- [ ] Update TaskJdbcRepository.java (update INSERT/UPDATE queries + add updateAssignedUser)
- [ ] Update TaskServiceJdbcImpl.java (add assignUser method)
- [ ] Update TaskAddRequest.java (add assignedUserId field)
- [ ] Update TaskEditRequest.java (add assignedUserId field)
- [ ] Create TaskAssignUserRequest.java (new request DTO)
- [ ] Update TaskService.java interface (add assignUser method)
- [ ] Update TaskController.java (add /assign-user endpoint)
- [ ] Update TaskIntegrationTests.java (update generateRandomTask + add new tests)
- [ ] Run validation gates (all builds and tests)

## Success Criteria

**The PRP is successfully implemented when:**
1. ✅ All tests pass for BOTH jpa and jdbc profiles
2. ✅ Application runs successfully with both profiles
3. ✅ Schema includes assigned_user_id column with FK constraint
4. ✅ Tasks can be created with/without assigned user
5. ✅ Assigned user can be changed (null → user, user → user, user → null)
6. ✅ Invalid user assignment returns 404
7. ✅ Swagger UI documents new endpoint
8. ✅ GET /task/{id} returns assignedUserId field
9. ✅ No existing functionality is broken

## PRP Confidence Score: 9/10

**Why 9/10:**
- ✅ Complete file-by-file implementation plan with exact line numbers
- ✅ Exact code patterns from existing similar feature (projectId)
- ✅ External documentation URLs with context
- ✅ Clear validation gates for both profiles
- ✅ All edge cases documented
- ✅ Both JPA and JDBC patterns fully specified
- ✅ Comprehensive test scenarios (8 new tests)
- ✅ Clear implementation order
- ✅ All gotchas and critical notes documented
- ⚠️ Minor risk: Possible variations in how different developers might structure the TaskEditRequest update handling

**What would make it 10/10:**
- Running the implementation and documenting any unexpected issues
- Including screenshots from Swagger UI
- Performance testing with large datasets
