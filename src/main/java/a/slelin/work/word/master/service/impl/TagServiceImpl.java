package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.tag.TagRequest;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.entity.Tag;
import a.slelin.work.word.master.exception.DuplicateResourceException;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.tag.TagMapper;
import a.slelin.work.word.master.repository.TagRepository;
import a.slelin.work.word.master.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    private final TagMapper tagMapper;

    @Override
    public List<TagResponse> getAll() {
        return tagMapper.toDtoList(tagRepository.findAllByOrderByNameAsc());
    }

    @Override
    @Transactional
    public TagResponse create(TagRequest request) {
        String name = request.name().trim();
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("name", name);
        }

        return tagMapper.toDto(tagRepository.save(Tag.builder().name(name).build()));
    }

    @Override
    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundByIdException(Tag.class, id));

        String name = request.name().trim();
        if (tagRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("name", name);
        }

        tag.setName(name);
        return tagMapper.toDto(tag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new EntityNotFoundByIdException(Tag.class, id);
        }

        tagRepository.deleteById(id);
    }

    @Override
    public List<Tag> getEntitiesByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> unique = new LinkedHashSet<>(ids);
        List<Tag> tags = tagRepository.findAllById(unique);
        if (tags.size() != unique.size()) {
            Set<Long> found = new HashSet<>();
            tags.forEach(tag -> found.add(tag.getId()));
            Long missing = unique.stream().filter(id -> !found.contains(id)).findFirst().orElseThrow();
            throw new EntityNotFoundByIdException(Tag.class, missing);
        }

        return new ArrayList<>(tags);
    }
}
