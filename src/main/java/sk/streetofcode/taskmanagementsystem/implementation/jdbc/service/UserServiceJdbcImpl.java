package sk.streetofcode.taskmanagementsystem.implementation.jdbc.service;

import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.UserService;
import sk.streetofcode.taskmanagementsystem.api.request.UserAddRequest;
import sk.streetofcode.taskmanagementsystem.domain.User;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository.UserJdbcRepository;

import java.util.List;

@Service
public class UserServiceJdbcImpl implements UserService {

    private final UserJdbcRepository repository;

    public UserServiceJdbcImpl(UserJdbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public long add(UserAddRequest request) {
        return 0;
    }

    @Override
    public void delete(long id) {

    }

    @Override
    public User get(long id) {
        return null;
    }

    @Override
    public List<User> getAll() {
        return repository.getAll();
    }
}
