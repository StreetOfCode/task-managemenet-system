package sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import sk.streetofcode.taskmanagementsystem.api.exception.InternalErrorException;
import sk.streetofcode.taskmanagementsystem.api.exception.ResourceNotFoundException;
import sk.streetofcode.taskmanagementsystem.api.request.TaskAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.TaskEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Task;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.mapper.TaskRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class TaskJdbcRepository {
    private final JdbcTemplate jdbcTemplate;
    private final TaskRowMapper taskRowMapper;

    private static final Logger logger;
    private static final String INSERT;
    private static final String UPDATE;
    private static final String UPDATE_STATUS;
    private static final String UPDATE_PROJECT;
    private static final String DELETE;
    private static final String GET_ALL;
    private static final String GET_BY_ID;
    private static final String GET_ALL_BY_USER;
    private static final String GET_ALL_BY_PROJECT;
    private static final String DELETE_ALL_BY_USER;
    private static final String DELETE_ALL_BY_PROJECT;

    static {
        logger = LoggerFactory.getLogger(TaskJdbcRepository.class);
        INSERT = "INSERT INTO task(id, user_id, project_id, name, description, status, created_at) VALUES (next value for task_id_seq, ?, ?, ?, ?, ?, ?)";
        UPDATE = "UPDATE task SET name = ?, description = ?, status = ? WHERE id = ?";
        UPDATE_STATUS = "UPDATE task SET status = ? WHERE id = ?";
        UPDATE_PROJECT = "UPDATE task SET project_id = ? WHERE id = ?";
        DELETE = "DELETE FROM task WHERE id = ?";
        GET_ALL = "SELECT * FROM task";
        GET_BY_ID = "SELECT * FROM task WHERE id = ?";
        GET_ALL_BY_USER = "SELECT * FROM task WHERE user_id = ?";
        GET_ALL_BY_PROJECT = "SELECT * FROM task WHERE project_id = ?";
        DELETE_ALL_BY_USER = "DELETE FROM task WHERE user_id = ?";
        DELETE_ALL_BY_PROJECT = "DELETE FROM task WHERE project_id = ?";
    }

    public TaskJdbcRepository(JdbcTemplate jdbcTemplate, TaskRowMapper taskRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskRowMapper = taskRowMapper;
    }

    public long add(TaskAddRequest request) {
        try {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                final PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, request.getUserId());
                if (request.getProjectId() != null) {
                    ps.setLong(2, request.getProjectId());
                } else {
                    ps.setNull(2, java.sql.Types.BIGINT);
                }
                ps.setString(3, request.getName());
                if (request.getDescription() != null) {
                    ps.setString(4, request.getDescription());
                } else {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                }
                ps.setString(5, TaskStatus.NEW.toString());
                ps.setTimestamp(6, Timestamp.from(OffsetDateTime.now().toInstant()));
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() == null) {
                logger.error("Error while adding task, keyHolder.getKey() is null");
                throw new InternalErrorException("Error while adding task");
            }

            return keyHolder.getKey().longValue();
        } catch (DataAccessException e) {
            logger.error("Error while adding task", e);
            throw new InternalErrorException("Error while adding task");
        }
    }

    public void update(long id, TaskEditRequest request) {
        try {
            jdbcTemplate.update(UPDATE, request.getName(), request.getDescription(), request.getStatus().toString(), id);
        } catch (DataAccessException e) {
            logger.error("Error while updating task", e);
            throw new InternalErrorException("Error while updating task");
        }
    }

    public void updateStatus(long id, TaskStatus status) {
        try {
            jdbcTemplate.update(UPDATE_STATUS, status.toString(), id);
        } catch (DataAccessException e) {
            logger.error("Error while updating task status", e);
            throw new InternalErrorException("Error while updating task status");
        }
    }

    public void updateProject(long id, long projectId) {
        try {
            jdbcTemplate.update(UPDATE_PROJECT, projectId, id);
        } catch (DataAccessException e) {
            logger.error("Error while updating task project", e);
            throw new InternalErrorException("Error while updating task project");
        }
    }

    public void delete(long id) {
        try {
            jdbcTemplate.update(DELETE, id);
        } catch (DataAccessException e) {
            logger.error("Error while deleting task", e);
            throw new InternalErrorException("Error while deleting task");
        }
    }

    public List<Task> getAll() {
        try {
            return jdbcTemplate.query(GET_ALL, taskRowMapper);
        } catch (DataAccessException e) {
            logger.error("Error while getting all tasks", e);
            throw new InternalErrorException("Error while getting all tasks");
        }
    }

    public Task getById(long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID, taskRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Task with id " + id + " not found");
        } catch (DataAccessException e) {
            logger.error("Error while getting task", e);
            throw new InternalErrorException("Error while getting task");
        }
    }

    public List<Task> getAllByUser(long userId) {
        try {
            return jdbcTemplate.query(GET_ALL_BY_USER, taskRowMapper, userId);
        } catch (DataAccessException e) {
            logger.error("Error while getting all tasks by user", e);
            throw new InternalErrorException("Error while getting all tasks by user");
        }
    }

    public List<Task> getAllByProject(long projectId) {
        try {
            return jdbcTemplate.query(GET_ALL_BY_PROJECT, taskRowMapper, projectId);
        } catch (DataAccessException e) {
            logger.error("Error while getting all tasks by project", e);
            throw new InternalErrorException("Error while getting all tasks by project");
        }
    }

    public void deleteAllByUser(long userId) {
        try {
            jdbcTemplate.update(DELETE_ALL_BY_USER, userId);
        } catch (DataAccessException e) {
            logger.error("Error while deleting all tasks by user", e);
            throw new InternalErrorException("Error while deleting all tasks by user");
        }
    }

    public void deleteAllByProject(long projectId) {
        try {
            jdbcTemplate.update(DELETE_ALL_BY_PROJECT, projectId);
        } catch (DataAccessException e) {
            logger.error("Error while deleting all tasks by project", e);
            throw new InternalErrorException("Error while deleting all tasks by project");
        }
    }
}
