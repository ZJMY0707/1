package studentknowledge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studentknowledge.dto.*;
import studentknowledge.model.Task;
import studentknowledge.model.enums.TaskStatus;
import studentknowledge.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }

    @GetMapping
    public ApiResponse<List<Task>> list(Authentication auth) {
        return ApiResponse.ok(service.listByUser(uid(auth)));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Task>> listByStatus(@PathVariable String status, Authentication auth) {
        return ApiResponse.ok(service.listByStatus(uid(auth), TaskStatus.valueOf(status)));
    }

    @PostMapping
    public ApiResponse<Task> create(@Valid @RequestBody TaskDTO dto, Authentication auth) {
        return ApiResponse.ok("创建成功", service.create(dto, uid(auth)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Task> update(@PathVariable Long id,
            @Valid @RequestBody TaskDTO dto, Authentication auth) {
        return ApiResponse.ok("更新成功", service.update(id, dto, uid(auth)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, uid(auth));
        return ApiResponse.ok("删除成功", null);
    }
}
