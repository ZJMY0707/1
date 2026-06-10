package studentknowledge.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String role;
}
