package com.century.web;

import com.century.report.StringResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.century.report.StringResult.errResult;

@Slf4j
@RestController
abstract class AbstractController {
    @ResponseStatus(value = HttpStatus.EXPECTATION_FAILED)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public StringResult handleConflict(Exception ex) {
        log.error("An error occurred in {}: " + ex.getMessage(), getClassName(), ex);
        return errResult(ex);
    }

    abstract String getClassName();
}
