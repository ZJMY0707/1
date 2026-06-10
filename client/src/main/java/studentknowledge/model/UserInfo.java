package studentknowledge.model;

public class UserInfo {
    public Long id;
    public String username;
    public String email;
    public String role;
    public String createdAt;
    public String updatedAt;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
}