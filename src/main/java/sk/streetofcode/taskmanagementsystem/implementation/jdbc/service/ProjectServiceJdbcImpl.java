package sk.streetofcode.taskmanagementsystem.implementation.jdbc.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.ProjectService;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Project;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository.ProjectJdbcRepository;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository.TaskJdbcRepository;
import sk.streetofcode.taskmanagementsystem.implementation.jdbc.repository.UserJdbcRepository;

import java.util.List;

@Service
@Profile("jdbc")
public class ProjectServiceJdbcImpl implements ProjectService {
    private final ProjectJdbcRepository repository;
    private final UserJdbcRepository userJdbcRepository;
    private final TaskJdbcRepository taskJdbcRepository;

    public ProjectServiceJdbcImpl(ProjectJdbcRepository repository, UserJdbcRepository userJdbcRepository, TaskJdbcRepository taskJdbcRepository) {
        this.repository = repository;
        this.userJdbcRepository = userJdbcRepository;
        this.taskJdbcRepository = taskJdbcRepository;
    }

    @Override
    public long add(ProjectAddRequest request) {
        return repository.add(request);
    }

    @Override
    public void edit(long id, ProjectEditRequest request) {
        if (this.get(id) != null) {
            repository.update(id, request);
        }
    }

    @Override
    public void delete(long id) {
        if (this.get(id) != null) {
            taskJdbcRepository.deleteAllByProject(id);
            repository.delete(id);
        }
    }

    @Override
    public Project get(long id) {
        return repository.getById(id);
    }

    @Override
    public List<Project> getAll() {
        return repository.getAll();
    }

    @Override
    public List<Project> getAllByUser(long userId) {
        if (userJdbcRepository.getById(userId) != null) {
            return repository.getAllByUser(userId);
        }

        return null;
    }
}
