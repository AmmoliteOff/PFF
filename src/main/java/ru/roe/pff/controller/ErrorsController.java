package ru.roe.pff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.roe.pff.dto.in.ErrorSolveDto;
import ru.roe.pff.service.ErrorService;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/error")
public class ErrorsController {
    private final ErrorService errorService;

    @PostMapping
    public void fix(@RequestBody ErrorSolveDto fixDto){
        errorService.fix(fixDto);
    }
}
