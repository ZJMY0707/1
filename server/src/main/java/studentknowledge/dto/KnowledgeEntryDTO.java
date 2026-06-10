package studentknowledge.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class KnowledgeEntryDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200字符")
    private String title;

    private String content;
    private Long categoryId;
    private Boolean isPinned;
    private List<Long> tagIds;
}
