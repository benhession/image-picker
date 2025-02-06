package com.benhession.imagepicker.localenvqueuehelper.http;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
public interface ImageProcessorClient extends SqsLambdaHttpClient {

    @POST
    String sendMessage(SQSEvent event);
}
