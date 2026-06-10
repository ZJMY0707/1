package studentknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studentknowledge.dto.StatsDTO;
import studentknowledge.model.enums.TaskStatus;
import studentknowledge.repository.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepo;
    private final KnowledgeEntryRepository entryRepo;
    private final TaskRepository taskRepo;

    /** 学生个人统计 */
    public StatsDTO userStats(Long userId) {
        return StatsDTO.builder()
                .totalEntries(entryRepo.countByUserId(userId))
                .totalTasks(taskRepo.countByUserId(userId))
                .completedTasks(taskRepo.countByUserIdAndStatus(userId, TaskStatus.DONE))
                .overdueTasks(taskRepo.countByUserIdAndStatus(userId, TaskStatus.OVERDUE))
                .categoryStats(entryRepo.countByCategory(userId))
                .taskStatusStats(taskRepo.countByStatusGrouped(userId))
                .build();
    }

    /** 管理员全局统计 */
    public StatsDTO adminStats() {
        return StatsDTO.builder()
                .totalUsers(userRepo.count())
                .totalEntries(entryRepo.count())
                .totalTasks(taskRepo.count())
                .categoryStats(entryRepo.countByCategoryAll())
                .taskStatusStats(taskRepo.countByStatusGroupedAll())
                .build();
    }
}
