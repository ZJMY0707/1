package studentknowledge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import studentknowledge.dto.ApiResponse;
import studentknowledge.model.User;
import studentknowledge.service.KnowledgeService;
import studentknowledge.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final KnowledgeService knowledgeService;

    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.ok(userService.findAll());
    }

    @PutMapping("/users/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.getOrDefault("password", "123456"));
        return ApiResponse.ok("密码已重置", null);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.ok("用户已删除", null);
    }

    @DeleteMapping("/knowledge/{id}")
    public ApiResponse<Void> deleteEntry(@PathVariable Long id) {
        knowledgeService.adminDelete(id);
        return ApiResponse.ok("已删除违规数据", null);
    }
}
