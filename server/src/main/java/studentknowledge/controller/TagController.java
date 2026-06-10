package studentknowledge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studentknowledge.dto.ApiResponse;
import studentknowledge.model.Tag;
import studentknowledge.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService service;

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }

    @GetMapping
    public ApiResponse<List<Tag>> list(Authentication auth) {
        return ApiResponse.ok(service.listByUser(uid(auth)));
    }

    @PostMapping
    public ApiResponse<Tag> create(@RequestBody Tag tag, Authentication auth) {
        tag.setUserId(uid(auth));
        return ApiResponse.ok("创建成功", service.save(tag));
    }

    @PutMapping("/{id}")
    public ApiResponse<Tag> update(@PathVariable Long id, @RequestBody Tag tag, Authentication auth) {
        return ApiResponse.ok("更新成功", service.update(id, tag, uid(auth)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, uid(auth));
        return ApiResponse.ok("删除成功", null);
    }
}
