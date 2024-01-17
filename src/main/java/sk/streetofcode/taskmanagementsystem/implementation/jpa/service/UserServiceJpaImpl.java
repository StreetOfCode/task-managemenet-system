package sk.streetofcode.taskmanagementsystem.implementation.jpa.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.UserService;
import sk.streetofcode.taskmanagementsystem.api.request.UserAddRequest;
import sk.streetofcode.taskmanagementsystem.domain.User;

import java.util.List;

@Service
@Primary
public class UserServiceJpaImpl implements UserService {
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
        return null;
    }
}
