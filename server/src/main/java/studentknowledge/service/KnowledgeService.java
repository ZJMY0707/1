package studentknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studentknowledge.dto.KnowledgeEntryDTO;
import studentknowledge.exception.BizException;
import studentknowledge.model.KnowledgeEntry;
import studentknowledge.model.Tag;
import studentknowledge.repository.KnowledgeEntryRepository;
import studentknowledge.repository.TagRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeEntryRepository entryRepo;
    private final TagRepository tagRepo;

    public List<KnowledgeEntry> listByUser(Long userId) {
        return entryRepo.findByUserIdOrderByIsPinnedDescCreatedAtDesc(userId);
    }

    public List<KnowledgeEntry> listByCategory(Long userId, Long categoryId) {
        return entryRepo.findByUserIdAndCategoryIdOrderByCreatedAtDesc(userId, categoryId);
    }

    public List<KnowledgeEntry> search(Long userId, String keyword) {
        return entryRepo.findByUserIdAndTitleContainingIgnoreCase(userId, keyword);
    }

    public KnowledgeEntry getById(Long id, Long userId) {
        KnowledgeEntry e = entryRepo.findById(id)
                .orElseThrow(() -> new BizException("笔记不存在"));
        if (!e.getUserId().equals(userId)) throw new BizException("无权访问");
        return e;
    }

    @Transactional
    public KnowledgeEntry create(KnowledgeEntryDTO dto, Long userId) {
        KnowledgeEntry entry = KnowledgeEntry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .userId(userId)
                .categoryId(dto.getCategoryId())
                .isPinned(dto.getIsPinned() != null && dto.getIsPinned())
                .build();
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepo.findAllById(dto.getTagIds()));
            entry.setTags(tags);
        }
        return entryRepo.save(entry);
    }

    @Transactional
    public KnowledgeEntry update(Long id, KnowledgeEntryDTO dto, Long userId) {
        KnowledgeEntry entry = entryRepo.findById(id)
                .orElseThrow(() -> new BizException("笔记不存在"));
        if (!entry.getUserId().equals(userId)) throw new BizException("无权操作");

        entry.setTitle(dto.getTitle());
        entry.setContent(dto.getContent());
        entry.setCategoryId(dto.getCategoryId());
        if (dto.getIsPinned() != null) entry.setIsPinned(dto.getIsPinned());
        if (dto.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepo.findAllById(dto.getTagIds()));
            entry.setTags(tags);
        }
        return entryRepo.save(entry);
    }

    public void delete(Long id, Long userId) {
        KnowledgeEntry entry = entryRepo.findById(id)
                .orElseThrow(() -> new BizException("笔记不存在"));
        if (!entry.getUserId().equals(userId)) throw new BizException("无权操作");
        entryRepo.deleteById(id);
    }

    /** 管理员删除 */
    public void adminDelete(Long id) {
        entryRepo.deleteById(id);
    }
}
