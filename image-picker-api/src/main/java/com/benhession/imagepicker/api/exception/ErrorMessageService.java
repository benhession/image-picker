package com.benhession.imagepicker.api.exception;

import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException;
import com.benhession.imagepicker.common.exception.AbstractSingleErrorApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ErrorMessageService {

    private final Logger log;

    public ErrorResponse mapApplicationExceptionList(AbstractMultipleErrorApplicationException exception) {
        String errorId = UUID.randomUUID().toString();
        log.warn("errorId[{}]", errorId, exception);

        List<ErrorResponse.ErrorMessage> errorMessages = exception.getErrorMessages()
          .stream()
          .map(error -> ErrorResponse.ErrorMessage.builder()
            .message(error.message())
            .path(error.path())
            .build())
          .toList();

        return ErrorResponse.builder()
          .errorId(errorId)
          .errors(errorMessages)
          .build();
    }

    public ErrorResponse mapSingleMessageError(AbstractSingleErrorApplicationException e) {
        String errorId = UUID.randomUUID().toString();
        log.error("errorId[{}]", errorId, e);

        return ErrorResponse.builder()
            .errorId(errorId)
            .errors(List.of(ErrorResponse.ErrorMessage.builder()
                .message(e.getMessage())
                .build()))
            .build();
    }
}
