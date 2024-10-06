package com.benhession.imagepicker.imageprocessor.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageCreationQueueHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private final Logger log;
    private final Instance<ImageProcessingMessageHandler> messsageHandlerInstance;

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        log.info("Received event: " + event);
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            var messageHandler = messsageHandlerInstance.get();
            try {
                messageHandler.handleMessage(msg);
            } catch (Exception e) {
                log.error("Failed to process message: " + msg.getMessageId(), e);
                failures.add(SQSBatchResponse.BatchItemFailure.builder()
                  .withItemIdentifier(msg.getMessageId())
                  .build());
            } finally {
                messsageHandlerInstance.destroy(messageHandler);
            }
        }
        return SQSBatchResponse.builder().withBatchItemFailures(failures).build();
    }
}
