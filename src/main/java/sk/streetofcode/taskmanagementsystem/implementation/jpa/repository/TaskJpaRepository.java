package sk.streetofcode.taskmanagementsystem.implementation.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.entity.TaskEntity;

import java.util.List;

@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByProjectId(Long projectId);

    List<TaskEntity> findAllByUserId(Long userId);
}
