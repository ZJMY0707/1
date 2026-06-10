package studentknowledge.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryNode {
    public Long id;
    public String name;
    public Long parentId;
    public Long userId;
    public Integer sortOrder;
    public List<CategoryNode> children = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getParentId() { return parentId; }
    public Integer getSortOrder() { return sortOrder; }
    @Override public String toString() { return name; }
}