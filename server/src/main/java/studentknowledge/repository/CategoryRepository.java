package studentknowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studentknowledge.model.Category;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderBySortOrder(Long userId);
    List<Category> findByUserIdAndParentIdIsNullOrderBySortOrder(Long userId);
    List<Category> findByUserIdAndParentIdOrderBySortOrder(Long userId, Long parentId);
}
