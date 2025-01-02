package com.benhession.imagepicker.localenvqueuehelper.listeners;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.benhession.imagepicker.localenvqueuehelper.config.SqsConfigProperties;
import com.benhession.imagepicker.localenvqueuehelper.http.ImageProcessorClient;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@ApplicationScoped
@RequiredArgsConstructor
public class ProcessingQueueListener {

    private final SqsConfigProperties sqsConfigProperties;
    private final SqsClient sqsClient;

    @RestClient
    ImageProcessorClient imageProcessorClient;

    @Scheduled(every = "5s")
    public void checkForMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
            .queueUrl(sqsConfigProperties.getProcessingQueueUrl())
            .maxNumberOfMessages(10)
            .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        for (Message message : messages) {
            SQSEvent sqsEvent = new SQSEvent();
            SQSMessage sqsMessage = new SQSMessage();
            sqsMessage.setMessageId(message.messageId());
            sqsMessage.setBody(message.body());
            sqsEvent.setRecords(List.of(sqsMessage));
            imageProcessorClient.sendMessage(sqsEvent);

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(sqsConfigProperties.getProcessingQueueUrl())
                .receiptHandle(message.receiptHandle())
                .build());
        }

    }
}
