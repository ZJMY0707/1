package studentknowledge.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class StatsDTO {
    private long totalUsers;
    private long totalEntries;
    private long totalTasks;
    private long completedTasks;
    private long overdueTasks;
    private List<Map<String, Object>> categoryStats;   // 分类统计
    private List<Map<String, Object>> taskStatusStats;  // 任务状态统计
}
