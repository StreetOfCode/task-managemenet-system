package sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import sk.streetofcode.taskmanagementsystem.api.exception.BadRequestException;
import sk.streetofcode.taskmanagementsystem.api.exception.InternalErrorException;
import sk.streetofcode.taskmanagementsystem.api.exception.ResourceNotFoundException;
import sk.streetofcode.taskmanagementsystem.api.request.UserAddRequest;
import sk.streetofcode.taskmanagementsystem.domain.User;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserJdbcRepository {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private static final Logger logger;

    private static final String GET_ALL;
    private static final String GET_BY_ID;

    private static final String INSERT;


    static {
        logger = LoggerFactory.getLogger(UserJdbcRepository.class);
        GET_ALL = "SELECT * FROM user";
        GET_BY_ID = "SELECT * FROM user WHERE id = ?";
        INSERT = "INSERT INTO user(id, name, email) VALUES (next value for user_id_seq, ?, ?)";
    }

    public UserJdbcRepository(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    public long add(UserAddRequest request) {
        try {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                final PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, request.getName());
                ps.setString(2, request.getEmail());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() == null) {
                logger.error("Error while adding user, keyHolder.getKey() is null");
                throw new InternalErrorException("Error while adding user");
            }

            return keyHolder.getKey().longValue();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        } catch (DataAccessException e) {
            logger.error("Error while adding user", e);
            throw new InternalErrorException("Error while adding user");
        }
    }

    public List<User> getAll() {
        return jdbcTemplate.query(GET_ALL, userRowMapper);
    }

    public User getById(long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        } catch (DataAccessException e) {
            logger.error("Error while getting user", e);
            throw new InternalErrorException("Error while getting user");
        }
    }
}
