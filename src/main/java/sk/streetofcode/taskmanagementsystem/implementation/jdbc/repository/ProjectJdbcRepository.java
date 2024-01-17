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
import sk.streetofcode.taskmanagementsystem.api.request.ProjectAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Project;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.mapper.ProjectRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class ProjectJdbcRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ProjectRowMapper projectRowMapper;

    private static final Logger logger;
    private static final String GET_ALL;
    private static final String GET_BY_ID;
    private static final String GET_ALL_BY_USER;
    private static final String INSERT;
    private static final String UPDATE;
    private static final String DELETE;
    private static final String DELETE_ALL_BY_USER;

    static {
        logger = LoggerFactory.getLogger(ProjectJdbcRepository.class);
        GET_ALL = "SELECT * FROM project";
        GET_BY_ID = "SELECT * FROM project WHERE id = ?";
        GET_ALL_BY_USER = "SELECT * FROM project WHERE user_id = ?";
        INSERT = "INSERT INTO project(id, user_id, name, description, created_at) VALUES (next value for project_id_seq, ?, ?, ?, ?)";
        UPDATE = "UPDATE project SET name = ?, description = ? WHERE id = ?";
        DELETE = "DELETE FROM project WHERE id = ?";
        DELETE_ALL_BY_USER = "DELETE FROM project WHERE user_id = ?";
    }

    public ProjectJdbcRepository(JdbcTemplate jdbcTemplate, ProjectRowMapper projectRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectRowMapper = projectRowMapper;
    }

    public List<Project> getAll() {
        try {
            return jdbcTemplate.query(GET_ALL, projectRowMapper);
        } catch (DataAccessException e) {
            logger.error("Error while getting all projects", e);
            throw new InternalErrorException("Error while getting all projects");
        }
    }

    public Project getById(long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID, projectRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Project with id " + id + " not found");
        } catch (DataAccessException e) {
            logger.error("Error while getting project", e);
            throw new InternalErrorException("Error while getting project");
        }
    }

    public List<Project> getAllByUser(long userId) {
        try {
            return jdbcTemplate.query(GET_ALL_BY_USER, projectRowMapper, userId);
        } catch (DataAccessException e) {
            logger.error("Error while getting all projects by user", e);
            throw new InternalErrorException("Error while getting all projects by user");
        }
    }

    public long add(ProjectAddRequest request) {
        try {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                final PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, request.getUserId());
                ps.setString(2, request.getName());
                if (request.getDescription() != null) {
                    ps.setString(3, request.getDescription());
                } else {
                    ps.setNull(3, java.sql.Types.VARCHAR);
                }
                ps.setTimestamp(4, Timestamp.from(OffsetDateTime.now().toInstant()));
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() == null) {
                logger.error("Error while adding project, keyHolder.getKey() is null");
                throw new InternalErrorException("Error while adding project");
            }

            return keyHolder.getKey().longValue();
        } catch (DataAccessException e) {
            logger.error("Error while adding project", e);
            throw new InternalErrorException("Error while adding project");
        }
    }

    public void update(long id, ProjectEditRequest request) {
        try {
            jdbcTemplate.update(UPDATE, request.getName(), request.getDescription(), id);
        } catch (DataAccessException e) {
            logger.error("Error while updating project", e);
            throw new InternalErrorException("Error while updating project");
        }
    }

    public void delete(long id) {
        try {
            jdbcTemplate.update(DELETE, id);
        } catch (DataAccessException e) {
            logger.error("Error while deleting project", e);
            throw new InternalErrorException("Error while deleting project");
        }
    }

    public void deleteAllByUser(long userId) {
        try {
            jdbcTemplate.update(DELETE_ALL_BY_USER, userId);
        } catch (DataAccessException e) {
            logger.error("Error while deleting all projects by user", e);
            throw new InternalErrorException("Error while deleting all projects by user");
        }
    }
}
