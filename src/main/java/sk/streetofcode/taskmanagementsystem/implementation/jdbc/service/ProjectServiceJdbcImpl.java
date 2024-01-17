package sk.streetofcode.taskmanagementsystem.implementation.jdbc.service;

import org.springframework.stereotype.Service;
import sk.streetofcode.taskmanagementsystem.api.ProjectService;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectAddRequest;
import sk.streetofcode.taskmanagementsystem.api.request.ProjectEditRequest;
import sk.streetofcode.taskmanagementsystem.domain.Project;

import java.util.List;

@Service
public class ProjectServiceJdbcImpl implements ProjectService {
    @Override
    public long add(ProjectAddRequest request) {
        return 0;
    }

    @Override
    public void edit(long id, ProjectEditRequest request) {

    }

    @Override
    public void delete(long id) {

    }

    @Override
    public Project get(long id) {
        return null;
    }

    @Override
    public List<Project> getAll() {
        return null;
    }

    @Override
    public List<Project> getAllByUser(long userId) {
        return null;
    }
}
