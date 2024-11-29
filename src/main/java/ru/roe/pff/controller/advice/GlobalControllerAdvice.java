package ru.roe.pff.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.roe.pff.exception.ApiException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(ApiException e) {
        log.error("Handled exception", e);
        var details = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        details.setTitle("Bad request");
        details.setDetail(e.getMessage());
        return ResponseEntity.status(details.getStatus()).body(details);
    }

}
