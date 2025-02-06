package com.benhession.imagepicker.localenvqueuehelper.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
public class SqsConfigProperties {

    @ConfigProperty(name = "queue.image-processing-queue.url")
    @SuppressWarnings("unused")
    String processingQueueUrl;

    @ConfigProperty(name = "queue.image-cropping-queue.url")
    @SuppressWarnings("unused")
    String croppingQueueUrl;
}
