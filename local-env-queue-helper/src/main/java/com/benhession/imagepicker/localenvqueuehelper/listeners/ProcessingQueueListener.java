package com.benhession.imagepicker.localenvqueuehelper.listeners;

import com.benhession.imagepicker.localenvqueuehelper.config.SqsConfigProperties;
import com.benhession.imagepicker.localenvqueuehelper.http.ImageProcessorClient;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@ApplicationScoped
@RequiredArgsConstructor
public class ProcessingQueueListener extends AbstractQueueListener {

    private final SqsConfigProperties sqsConfigProperties;
    private final SqsClient sqsClient;

    @RestClient
    ImageProcessorClient imageProcessorClient;

    @Scheduled(every = "5s")
    public void checkForMessages() {
        super.checkForMessages(sqsConfigProperties.getProcessingQueueUrl(), sqsClient, imageProcessorClient);
    }

}
