package studentknowledge.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "标题最多200字符")
    private String title;

    private String description;
    private String status;
    private String priority;
    private LocalDateTime startTime;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
}
