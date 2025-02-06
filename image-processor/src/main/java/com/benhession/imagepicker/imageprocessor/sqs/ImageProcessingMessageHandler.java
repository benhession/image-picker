package com.benhession.imagepicker.imageprocessor.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.benhession.imagepicker.common.security.UserInfo;
import com.benhession.imagepicker.common.security.UserInfoProducer;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.benhession.imagepicker.imageprocessor.controller.ImageProcessingController;
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
public class ImageProcessingMessageHandler {

    private final UserInfoProducer userInfoProducer;
    private final ImageProcessingController imageProcessingController;

    @Inject
    @Named("imageCreationMessageReader")
    ObjectReader creationMessageReader;

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
