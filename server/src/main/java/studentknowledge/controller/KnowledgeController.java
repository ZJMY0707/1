package studentknowledge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import studentknowledge.dto.*;
import studentknowledge.model.KnowledgeEntry;
import studentknowledge.service.KnowledgeService;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService service;

    private Long uid(Authentication auth) { return (Long) auth.getPrincipal(); }

    @GetMapping
    public ApiResponse<List<KnowledgeEntry>> list(Authentication auth) {
        return ApiResponse.ok(service.listByUser(uid(auth)));
    }

    @GetMapping("/category/{catId}")
    public ApiResponse<List<KnowledgeEntry>> listByCategory(@PathVariable Long catId, Authentication auth) {
        return ApiResponse.ok(service.listByCategory(uid(auth), catId));
    }

    @GetMapping("/search")
    public ApiResponse<List<KnowledgeEntry>> search(@RequestParam String keyword, Authentication auth) {
        return ApiResponse.ok(service.search(uid(auth), keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeEntry> detail(@PathVariable Long id, Authentication auth) {
        return ApiResponse.ok(service.getById(id, uid(auth)));
    }

    @PostMapping
    public ApiResponse<KnowledgeEntry> create(@Valid @RequestBody KnowledgeEntryDTO dto, Authentication auth) {
        return ApiResponse.ok("创建成功", service.create(dto, uid(auth)));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeEntry> update(@PathVariable Long id,
            @Valid @RequestBody KnowledgeEntryDTO dto, Authentication auth) {
        return ApiResponse.ok("更新成功", service.update(id, dto, uid(auth)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, uid(auth));
        return ApiResponse.ok("删除成功", null);
    }
}
