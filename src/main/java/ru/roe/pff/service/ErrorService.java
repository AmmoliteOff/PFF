package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.roe.pff.dto.in.ErrorSolveDto;
import ru.roe.pff.dto.out.FileErrorDto;
import ru.roe.pff.dto.out.PagesCountDto;
import ru.roe.pff.entity.ErrorSolve;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.enums.ErrorType;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final FileErrorRepository fileErrorRepository;
    private final FileRepository fileRepository;

    public void fix(ErrorSolveDto errorSolveDto) {
        var error = fileErrorRepository.findById(errorSolveDto.errorId()).orElseThrow();
        var solve = new ErrorSolve(null, errorSolveDto.value());
        error.setUseSolve(true);
        error.setErrorSolve(solve);
        fileErrorRepository.save(error);
    }

    public List<FileError> getErrorsByFileIdEntity(UUID id) {
        return fileErrorRepository.findAllByFeedFile(fileRepository.findById(id).orElseThrow());
    }

    public List<FileErrorDto> getErrorsByFileId(UUID id) {
        return fileErrorRepository.findAllByFeedFile(fileRepository.findById(id).orElseThrow())
            .stream()
            .map(o -> new FileErrorDto(
                o.getId(),
                o.getTitle(),
                o.getDescription(),
                o.getErrorSolve(),
                o.getErrorType(),
                o.getRowIndex(),
                o.getColumnIndex(),
                o.getSuppressed(),
                o.getUseSolve()
            )).toList();
    }

    public void suppressError(UUID id) {
        var error = fileErrorRepository.findById(id).orElseThrow();
        error.setSuppressed(true);
        fileErrorRepository.save(error);
    }

    public Page<FileError> getEntitiesPaginatedAndSortedByCreatedAtDesc(int page, int size, UUID id) {
        Pageable pageable = PageRequest.of(page, size);
        var feedFile = fileRepository.findById(id).orElseThrow();
        return (fileErrorRepository.findAllByFeedFileOrderByCreatedAtDesc(pageable, feedFile));
    }

    public void dropAll(UUID id) {
        var errors = getErrorsByFileIdEntity(id);
        for (var error : errors) {
            error.setUseSolve(false);
            if (error.getErrorType() != ErrorType.TECHNICAL) {
                error.setErrorSolve(null);
            }
        }
        fileErrorRepository.saveAll(errors);
    }

    public PagesCountDto getPagesCount(UUID id) {
        var feedFile = fileRepository.findById(id).orElseThrow();
        var errors = fileErrorRepository.findAllByFeedFile(feedFile);
        return new PagesCountDto((int) Math.ceil((double)errors.size()/10));
    }

    public void suppressAll(UUID fileId) {
        var feedFile = fileRepository.findById(fileId).orElseThrow();
        var errors = fileErrorRepository.findAllByFeedFile(feedFile);
        for(var error : errors) {
            if(!error.getUseSolve()) {
                error.setSuppressed(true);
            }
        }
        fileErrorRepository.saveAll(errors);
    }

    public void useAiForAll(UUID fileId) {
        var feedFile = fileRepository.findById(fileId).orElseThrow();
        var errors = fileErrorRepository.findAllByFeedFile(feedFile);
        for(var error : errors) {
            if(error.getErrorType() != ErrorType.AI) {
                if(!Objects.equals(error.getErrorSolve().getValue(), "")) {
                    error.setUseSolve(true);
                }
                else{
                    error.setSuppressed(true);
                }
            }
        }
        fileErrorRepository.saveAll(errors);
    }

    public Page<FileError> getErrorsFiltered(UUID id, int page, ErrorType errorType) {
        Pageable pageable = PageRequest.of(page, 10);
        var feedFile = fileRepository.findById(id).orElseThrow();
        return fileErrorRepository.findAllByFeedFileAndErrorTypeOrderByCreatedAtDesc(pageable, feedFile, errorType);
    }
}
