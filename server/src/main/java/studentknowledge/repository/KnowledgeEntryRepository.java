package studentknowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import studentknowledge.model.KnowledgeEntry;
import java.util.List;
import java.util.Map;

public interface KnowledgeEntryRepository extends JpaRepository<KnowledgeEntry, Long> {
    List<KnowledgeEntry> findByUserIdOrderByIsPinnedDescCreatedAtDesc(Long userId);
    List<KnowledgeEntry> findByUserIdAndCategoryIdOrderByCreatedAtDesc(Long userId, Long categoryId);
    List<KnowledgeEntry> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);
    long countByUserId(Long userId);

    @Query("SELECT c.name as name, COUNT(e) as count FROM KnowledgeEntry e " +
           "JOIN Category c ON e.categoryId = c.id WHERE e.userId = :userId GROUP BY c.name")
    List<Map<String, Object>> countByCategory(Long userId);

    @Query("SELECT c.name as name, COUNT(e) as count FROM KnowledgeEntry e " +
           "JOIN Category c ON e.categoryId = c.id GROUP BY c.name")
    List<Map<String, Object>> countByCategoryAll();
}
