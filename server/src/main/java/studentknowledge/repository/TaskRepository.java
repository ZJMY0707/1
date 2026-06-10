package studentknowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studentknowledge.model.Task;
import studentknowledge.model.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByDeadlineAsc(Long userId);
    List<Task> findByUserIdAndStatusOrderByDeadlineAsc(Long userId, TaskStatus status);
    List<Task> findByStatusAndDeadlineBefore(TaskStatus status, LocalDateTime time);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, TaskStatus status);

    @Query("SELECT t.status as name, COUNT(t) as count FROM Task t WHERE t.userId = :userId GROUP BY t.status")
    List<Map<String, Object>> countByStatusGrouped(Long userId);

    @Query("SELECT t.status as name, COUNT(t) as count FROM Task t GROUP BY t.status")
    List<Map<String, Object>> countByStatusGroupedAll();
}
