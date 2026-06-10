package studentknowledge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studentknowledge.exception.BizException;
import studentknowledge.model.Tag;
import studentknowledge.repository.TagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository repo;

    public List<Tag> listByUser(Long userId) { return repo.findByUserId(userId); }

    public Tag save(Tag tag) {
        if (repo.existsByUserIdAndName(tag.getUserId(), tag.getName())) {
            throw new BizException("标签名已存在");
        }
        return repo.save(tag);
    }

    public Tag update(Long id, Tag req, Long userId) {
        Tag tag = repo.findById(id).orElseThrow(() -> new BizException("标签不存在"));
        if (!tag.getUserId().equals(userId)) throw new BizException("无权操作");
        tag.setName(req.getName());
        tag.setColor(req.getColor());
        return repo.save(tag);
    }

    public void delete(Long id, Long userId) {
        Tag tag = repo.findById(id).orElseThrow(() -> new BizException("标签不存在"));
        if (!tag.getUserId().equals(userId)) throw new BizException("无权操作");
        repo.deleteById(id);
    }
}
