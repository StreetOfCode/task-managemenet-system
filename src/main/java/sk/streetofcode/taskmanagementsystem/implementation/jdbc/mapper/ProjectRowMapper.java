package sk.streetofcode.taskmanagementsystem.implementation.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import sk.streetofcode.taskmanagementsystem.domain.Project;

import java.time.ZoneOffset;

@Component
public class ProjectRowMapper implements RowMapper<Project> {
    @Override
    public Project mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Project(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime().atOffset(ZoneOffset.UTC)
        );
    }
}
