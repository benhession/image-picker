package com.benhession.imagepicker.imageprocessor.service.sqs;


import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SqsTestHelper {
    public static SQSBatchResponse sendSqsEvent(ImageCreationMessage imageCreationMessage)
        throws JsonProcessingException {

        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(new ArrayList<>());
        SQSEvent.SQSMessage msg = new SQSEvent.SQSMessage();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter objectWriter = objectMapper.writerFor(ImageCreationMessage.class);
        String messageBody = objectWriter.writeValueAsString(imageCreationMessage);
        msg.setBody(messageBody);
        msg.setMessageId(UUID.randomUUID().toString());
        sqsEvent.getRecords().add(msg);

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(sqsEvent)
            .when()
            .post()
            .then()
            .statusCode(200).extract().response().as(SQSBatchResponse.class);
    }
}
