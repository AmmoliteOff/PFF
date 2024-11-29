package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.roe.pff.dto.in.ErrorSolveDto;
import ru.roe.pff.dto.out.FileErrorDto;
import ru.roe.pff.entity.ErrorSolve;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final FileErrorRepository fileErrorRepository;
    private final FileRepository fileRepository;

    public void fix(ErrorSolveDto errorSolveDto) {
        var error = fileErrorRepository.findById(errorSolveDto.errorId()).orElseThrow();
        var solve = new ErrorSolve(null, errorSolveDto.value());
        error.setErrorSolve(solve);
        fileErrorRepository.save(error);
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
                        o.getSuppressed()
                )).toList();
    }
}
