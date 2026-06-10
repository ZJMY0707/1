package studentknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studentknowledge.exception.BizException;
import studentknowledge.model.Category;
import studentknowledge.repository.CategoryRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repo;

    /** 获取用户的分类树 */
    public List<Category> getTree(Long userId) {
        List<Category> all = repo.findByUserIdOrderBySortOrder(userId);
        Map<Long, Category> map = new LinkedHashMap<>();
        all.forEach(c -> { c.setChildren(new ArrayList<>()); map.put(c.getId(), c); });

        List<Category> roots = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentId() == null) {
                roots.add(c);
            } else {
                Category parent = map.get(c.getParentId());
                if (parent != null) parent.getChildren().add(c);
            }
        }
        return roots;
    }

    public List<Category> listFlat(Long userId) {
        return repo.findByUserIdOrderBySortOrder(userId);
    }

    public Category save(Category cat) { return repo.save(cat); }

    public Category update(Long id, Category req, Long userId) {
        Category cat = repo.findById(id)
                .orElseThrow(() -> new BizException("分类不存在"));
        if (!cat.getUserId().equals(userId)) throw new BizException("无权操作");
        cat.setName(req.getName());
        cat.setParentId(req.getParentId());
        cat.setSortOrder(req.getSortOrder());
        return repo.save(cat);
    }

    public void delete(Long id, Long userId) {
        Category cat = repo.findById(id)
                .orElseThrow(() -> new BizException("分类不存在"));
        if (!cat.getUserId().equals(userId)) throw new BizException("无权操作");
        repo.deleteById(id);
    }
}
