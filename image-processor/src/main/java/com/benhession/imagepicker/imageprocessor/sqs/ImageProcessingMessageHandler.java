package com.benhession.imagepicker.imageprocessor.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.imageprocessor.controller.ImageProcessingController;
import com.benhession.imagepicker.imageprocessor.security.UserInfo;
import com.benhession.imagepicker.imageprocessor.security.UserInfoProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@Dependent
@RequiredArgsConstructor
public class ImageProcessingMessageHandler {
    private final ObjectReader creationMessageReader;
    private final UserInfoProducer userInfoProducer;
    private final ImageProcessingController imageProcessingController;

    @ActivateRequestContext
    public void handleMessage(SQSEvent.SQSMessage message) throws JsonProcessingException {
        try {
            MDC.put("message.id", message.getMessageId());
            ImageCreationMessage creationMessage = creationMessageReader.readValue(message.getBody());
            UserInfo userInfo = userInfoProducer.init(creationMessage.getAuthJwt());
            MDC.put("requester", userInfo.getUserName());
            imageProcessingController.handleProcessingRequest(creationMessage);
        } finally {
            MDC.clear();
        }
    }
}
