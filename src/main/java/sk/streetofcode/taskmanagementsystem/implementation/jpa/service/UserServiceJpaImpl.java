package sk.streetofcode.taskmanagementsystem.implementation.jpa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.UserService;
import sk.streetofcode.taskmanagementsystem.api.exception.BadRequestException;
import sk.streetofcode.taskmanagementsystem.api.exception.InternalErrorException;
import sk.streetofcode.taskmanagementsystem.api.exception.ResourceNotFoundException;
import sk.streetofcode.taskmanagementsystem.api.request.UserAddRequest;
import sk.streetofcode.taskmanagementsystem.domain.User;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.entity.UserEntity;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.repository.UserJpaRepository;

import java.util.List;

@Service
@Primary
public class UserServiceJpaImpl implements UserService {
    private final UserJpaRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceJpaImpl.class);

    public UserServiceJpaImpl(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public long add(UserAddRequest request) {
        try {
            return repository.save(new UserEntity(request.getName(), request.getEmail())).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        } catch (DataAccessException e) {
            logger.error("Error while adding user", e);
            throw new InternalErrorException("Error while adding user");
        }
    }

    @Override
    public void delete(long id) {
        if (this.get(id) != null) {
            repository.deleteById(id);
        }
    }

    @Override
    public User get(long id) {
        return repository
                .findById(id).map(this::mapUserEntityToUser)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public List<User> getAll() {
        return repository.findAll().stream().map(this::mapUserEntityToUser).toList();
    }

    private User mapUserEntityToUser(UserEntity userEntity) {
        return new User(userEntity.getId(), userEntity.getName(), userEntity.getEmail());
    }
}
