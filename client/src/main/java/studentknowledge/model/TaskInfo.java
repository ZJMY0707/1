package studentknowledge.model;

public class TaskInfo {
    public Long id;
    public String title;
    public String description;
    public Long userId;
    public String status;
    public String priority;
    public String startTime;
    public String deadline;
    public String completedAt;
    public String createdAt;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getDeadline() { return deadline; }
    public String getCreatedAt() { return createdAt; }
}