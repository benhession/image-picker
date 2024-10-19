package com.benhession.imagepicker.api.sqs;

import com.benhession.imagepicker.api.config.SqsConfigProperties;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@ApplicationScoped
@RequiredArgsConstructor
public class ImageProcessingQueueService {

    private final ObjectWriter imageCreationMessageWriter;
    private final SqsClient sqsClient;
    private final Logger logger;
    private final SqsConfigProperties sqsConfigProperties;
    private final JsonWebToken jwt;

    public void sendMessage(ImageCreationMessage imageCreationMessage) throws ImageProcessingException {
        imageCreationMessage.setAuthJwt(jwt.toString());
        try {
            String message = imageCreationMessageWriter.writeValueAsString(imageCreationMessage);
            SendMessageResponse response = sqsClient.sendMessage(m -> m
                .queueUrl(sqsConfigProperties.getQueueUrl())
                .messageBody(message));
            logger.infov("Sent message to processing queue: messageId = {0}, metaDataId = {1}, fileDataKey = {2}",
                response.messageId(), imageCreationMessage.getMetaDataId(), imageCreationMessage.getFileDataKey());
        } catch (JsonProcessingException e) {
            logger.error("Error parsing image creation message", e);
            throw new ImageProcessingException("Error sending message to processing queue", e);
        }
    }
}
