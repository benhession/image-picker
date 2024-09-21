package com.benhession.imagepicker.api.exception;

import com.benhession.imagepicker.common.exception.AbstractMultipleErrorApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
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
}
