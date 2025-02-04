package com.benhession.imagepicker.cropper.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.benhession.imagepicker.common.security.UserInfo;
import com.benhession.imagepicker.common.security.UserInfoProducer;
import com.benhession.imagepicker.common.sqs.ImageCropMessage;
import com.benhession.imagepicker.cropper.controller.ImageCropController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@Dependent
@RequiredArgsConstructor
public class ImageCropMessageHandler {

    private final UserInfoProducer userInfoProducer;
    private final ImageCropController imageCropController;
    
    @Inject
    @Named("imageCropMessageReader")
    ObjectReader cropMessageReader;

    @ActivateRequestContext
    public void handleMessage(SQSEvent.SQSMessage message) throws JsonProcessingException {
        try {
            MDC.put("message.id", message.getMessageId());
            ImageCropMessage imageCropMessage = cropMessageReader.readValue(message.getBody());
            UserInfo userInfo = userInfoProducer.init(imageCropMessage.getAuthJwt());
            MDC.put("requester", userInfo.getUserName());
            imageCropController.handleCropQueueRequest(imageCropMessage);
        } finally {
            MDC.clear();
        }
    }
}
