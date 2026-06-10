package studentknowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studentknowledge.model.Tag;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
