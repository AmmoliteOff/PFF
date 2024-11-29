package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.roe.pff.dto.in.ErrorSolveDto;
import ru.roe.pff.entity.ErrorSolve;
import ru.roe.pff.repository.FileErrorRepository;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final FileErrorRepository fileErrorRepository;

    public void fix(ErrorSolveDto errorSolveDto) {
        var error = fileErrorRepository.findById(errorSolveDto.errorId()).orElseThrow();
        var solve = new ErrorSolve(null, errorSolveDto.value());
        error.setErrorSolve(solve);
        fileErrorRepository.save(error);
    }
}
