package sk.streetofcode.taskmanagementsystem.implementation.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sk.streetofcode.taskmanagementsystem.implementation.jpa.entity.ProjectEntity;

import java.util.List;

@Repository
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findAllByUserId(Long userId);
}
