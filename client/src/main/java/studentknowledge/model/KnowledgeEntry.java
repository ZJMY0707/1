package studentknowledge.model;

import java.util.Set;
import java.util.HashSet;

public class KnowledgeEntry {
    public Long id;
    public String title;
    public String content;
    public Long userId;
    public Long categoryId;
    public Boolean isPinned;
    public Set<TagInfo> tags = new HashSet<>();
    public String createdAt;
    public String updatedAt;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Long getCategoryId() { return categoryId; }
    public Boolean getIsPinned() { return isPinned; }
    public Set<TagInfo> getTags() { return tags; }
    public String getCreatedAt() { return createdAt; }
}