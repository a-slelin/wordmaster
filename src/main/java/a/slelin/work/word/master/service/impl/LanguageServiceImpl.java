package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.language.LanguageRequest;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.entity.Language;
import a.slelin.work.word.master.exception.BusinessFault;
import a.slelin.work.word.master.exception.DuplicateResourceException;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.language.LanguageMapper;
import a.slelin.work.word.master.repository.LanguageRepository;
import a.slelin.work.word.master.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    private final LanguageMapper languageMapper;

    @Override
    public List<LanguageResponse> getAll() {
        return languageMapper.toDtoList(languageRepository.findAllByOrderByIdAsc());
    }

    @Override
    @Transactional
    public LanguageResponse create(LanguageRequest request) {
        if (request.code() == null || request.name() == null) {
            throw new BusinessFault("Language code and name are required.");
        }

        if (languageRepository.existsByCodeIgnoreCase(request.code())) {
            throw new DuplicateResourceException("code", request.code());
        }

        Language language = languageMapper.toEntity(request);
        language.setCode(language.getCode().toLowerCase(Locale.ROOT));
        return languageMapper.toDto(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponse update(Long id, LanguageRequest request) {
        Language language = getEntityById(id);

        if (request.code() != null && languageRepository.existsByCodeIgnoreCaseAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("code", request.code());
        }

        languageMapper.patch(language, request);
        language.setCode(language.getCode().toLowerCase(Locale.ROOT));
        return languageMapper.toDto(language);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Language language = getEntityById(id);
        try {
            languageRepository.delete(language);
            languageRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessFault("Language is used by decks and can not be deleted.", e);
        }
    }

    @Override
    public Language getEntityById(Long id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundByIdException(Language.class, id));
    }
}
