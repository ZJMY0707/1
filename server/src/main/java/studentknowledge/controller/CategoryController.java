package studentknowledge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studentknowledge.dto.ApiResponse;
import studentknowledge.model.Category;
import studentknowledge.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }

    @GetMapping("/tree")
    public ApiResponse<List<Category>> tree(Authentication auth) {
        return ApiResponse.ok(service.getTree(uid(auth)));
    }

    @GetMapping
    public ApiResponse<List<Category>> list(Authentication auth) {
        return ApiResponse.ok(service.listFlat(uid(auth)));
    }

    @PostMapping
    public ApiResponse<Category> create(@RequestBody Category cat, Authentication auth) {
        cat.setUserId(uid(auth));
        return ApiResponse.ok("创建成功", service.save(cat));
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @RequestBody Category cat, Authentication auth) {
        return ApiResponse.ok("更新成功", service.update(id, cat, uid(auth)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, uid(auth));
        return ApiResponse.ok("删除成功", null);
    }
}
