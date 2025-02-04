package com.benhession.imagepicker.api.sqs;

import com.benhession.imagepicker.api.config.SqsConfigProperties;
import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.sqs.ImageCropMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.jwt.JsonWebToken;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@ApplicationScoped
@RequiredArgsConstructor
@JBossLog
public class ImageCroppingQueueService {

    private final JsonWebToken jwt;
    private final SqsConfigProperties sqsConfigProperties;
    private final SqsClient sqsClient;

    @Inject
    @Named("imageCropMessageWriter")
    ObjectWriter imageCropMessageWriter;
    
    public void sendMessage(ImageCropMessage imageCropMessage) throws ImageProcessingException {
        imageCropMessage.setAuthJwt(jwt.getRawToken());
        try {
            String message = imageCropMessageWriter.writeValueAsString(imageCropMessage);
            SendMessageResponse response = sqsClient.sendMessage(m -> m
                .queueUrl(sqsConfigProperties.getCroppingQueueUrl())
                .messageBody(message));
            log.infov("Sent message to cropping queue: messageId = {0}, metadataId = {1}, fileDataKey = {2}",
                response.messageId(), imageCropMessage.getMetaDataId(), imageCropMessage.getFileDataKey());
        } catch (JsonProcessingException e) {
            log.error("Error parsing image crop message", e);
            throw new ImageProcessingException("Error parsing image crop message", e);
        }
    }
}
