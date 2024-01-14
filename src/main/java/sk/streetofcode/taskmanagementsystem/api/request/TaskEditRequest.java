package sk.streetofcode.taskmanagementsystem.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.streetofcode.taskmanagementsystem.domain.TaskStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEditRequest {
    private String name;
    private String description;
    private TaskStatus status;
}
