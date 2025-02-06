package com.benhession.imagepicker.localenvqueuehelper.listeners;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.benhession.imagepicker.localenvqueuehelper.http.SqsLambdaHttpClient;
import java.util.List;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

public class AbstractQueueListener {

    public void checkForMessages(String queueUrl, SqsClient sqsClient, SqsLambdaHttpClient sqsLambdaHttpClient) {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        for (Message message : messages) {
            SQSEvent sqsEvent = new SQSEvent();
            SQSMessage sqsMessage = new SQSMessage();
            sqsMessage.setMessageId(message.messageId());
            sqsMessage.setBody(message.body());
            sqsEvent.setRecords(List.of(sqsMessage));
            sqsLambdaHttpClient.sendMessage(sqsEvent);

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
        }
    }
}
