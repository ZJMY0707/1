package studentknowledge.model;

public class TagInfo {
    public Long id;
    public String name;
    public String color;
    public Long userId;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    @Override public String toString() { return name; }
}