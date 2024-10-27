package com.benhession.imagepicker.api.sqs;

import static org.assertj.core.api.Assertions.assertThat;

import com.benhession.imagepicker.api.config.SqsConfigProperties;
import com.benhession.imagepicker.common.sqs.ImageCreationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@QuarkusTest
@RequiredArgsConstructor
public class ImageProcessingQueueServiceTest {

    private final ObjectReader imageCreationMessageReader;
    private final ImageProcessingQueueService imageProcessingQueueService;
    private final SqsClient sqsClient;
    private final SqsConfigProperties sqsConfigProperties;

    private String testFileDataKey;
    private String testMetaDataId;

    @BeforeEach
    public void init() {
        testFileDataKey = UUID.randomUUID().toString();
        testMetaDataId = new ObjectId().toString();
    }

    @AfterEach
    public void tearDown() {
        sqsClient.purgeQueue(PurgeQueueRequest.builder()
                .queueUrl(sqsConfigProperties.getQueueUrl())
            .build());
    }

    @Test
    public void When_SendMessage_Expect_MessageIsOnQueue() throws JsonProcessingException {
        // arrange
        var imageCreationMessage = ImageCreationMessage.builder()
            .fileDataKey(testFileDataKey)
            .metaDataId(testMetaDataId)
            .build();

        // act
        imageProcessingQueueService.sendMessage(imageCreationMessage);

        // assert
        var receiveMessageResponse = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(sqsConfigProperties.getQueueUrl())
                .maxNumberOfMessages(1)
                .build());

        List<Message> messages = receiveMessageResponse.messages();
        assertThat(messages).hasSize(1);

        Message message = messages.getFirst();
        assertThat(message.messageId()).isNotNull();

        ImageCreationMessage actualMessage = imageCreationMessageReader.readValue(message.body());

        assertThat(actualMessage.getFileDataKey()).isEqualTo(testFileDataKey);
        assertThat(actualMessage.getMetaDataId()).isEqualTo(testMetaDataId);
        assertThat(actualMessage.getAuthJwt()).isNotNull();
    }
}
