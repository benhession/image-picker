package com.benhession.imagepicker.imageprocessor.client;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.services.lambda.LambdaClient;

@ApplicationScoped
@SuppressWarnings("unused")
public class LambdaClientProducer {

    @Produces
    @Priority(Integer.MAX_VALUE)
    @Default
    public LambdaClient createLambdaClient() {
        return LambdaClient.create();
    }
}