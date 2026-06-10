package studentknowledge.model;

import java.util.List;
import java.util.Map;

public class StatsData {
    public long totalUsers;
    public long totalEntries;
    public long totalTasks;
    public long completedTasks;
    public long overdueTasks;
    public List<Map<String, Object>> categoryStats;
    public List<Map<String, Object>> taskStatusStats;
}
