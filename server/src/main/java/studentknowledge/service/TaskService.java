package studentknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import studentknowledge.dto.TaskDTO;
import studentknowledge.exception.BizException;
import studentknowledge.model.Task;
import studentknowledge.model.enums.Priority;
import studentknowledge.model.enums.TaskStatus;
import studentknowledge.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repo;

    public List<Task> listByUser(Long userId) {
        return repo.findByUserIdOrderByDeadlineAsc(userId);
    }

    public List<Task> listByStatus(Long userId, TaskStatus status) {
        return repo.findByUserIdAndStatusOrderByDeadlineAsc(userId, status);
    }

    public Task create(TaskDTO dto, Long userId) {
        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .userId(userId)
                .status(dto.getStatus() != null ? TaskStatus.valueOf(dto.getStatus()) : TaskStatus.TODO)
                .priority(dto.getPriority() != null ? Priority.valueOf(dto.getPriority()) : Priority.MEDIUM)
                .startTime(dto.getStartTime())
                .deadline(dto.getDeadline())
                .build();
        return repo.save(task);
    }

    public Task update(Long id, TaskDTO dto, Long userId) {
        Task task = repo.findById(id).orElseThrow(() -> new BizException("任务不存在"));
        if (!task.getUserId().equals(userId)) throw new BizException("无权操作");

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) task.setStatus(TaskStatus.valueOf(dto.getStatus()));
        if (dto.getPriority() != null) task.setPriority(Priority.valueOf(dto.getPriority()));
        task.setStartTime(dto.getStartTime());
        task.setDeadline(dto.getDeadline());

        if (task.getStatus() == TaskStatus.DONE && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        }
        return repo.save(task);
    }

    public void delete(Long id, Long userId) {
        Task task = repo.findById(id).orElseThrow(() -> new BizException("任务不存在"));
        if (!task.getUserId().equals(userId)) throw new BizException("无权操作");
        repo.deleteById(id);
    }

    /** 定时检查逾期 - 每小时执行 */
    @Scheduled(fixedRate = 3600000)
    public void checkOverdue() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdue = repo.findByStatusAndDeadlineBefore(TaskStatus.TODO, now);
        overdue.addAll(repo.findByStatusAndDeadlineBefore(TaskStatus.IN_PROGRESS, now));
        overdue.forEach(t -> t.setStatus(TaskStatus.OVERDUE));
        repo.saveAll(overdue);
    }
}
