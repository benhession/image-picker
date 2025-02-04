package com.benhession.imagepicker.localenvqueuehelper.listeners;

import com.benhession.imagepicker.localenvqueuehelper.config.SqsConfigProperties;
import com.benhession.imagepicker.localenvqueuehelper.http.ImageCropperClient;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@ApplicationScoped
@RequiredArgsConstructor
public class CropQueueListener extends AbstractQueueListener {

    private final SqsConfigProperties sqsConfigProperties;
    private final SqsClient sqsClient;

    @RestClient
    ImageCropperClient imageCropperClient;

    @Scheduled(every = "5s")
    public void checkForMessages() {
        super.checkForMessages(sqsConfigProperties.getCroppingQueueUrl(), sqsClient, imageCropperClient);
    }

}
