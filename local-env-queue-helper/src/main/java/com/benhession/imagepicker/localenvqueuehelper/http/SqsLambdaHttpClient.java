package com.benhession.imagepicker.localenvqueuehelper.http;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public interface SqsLambdaHttpClient {

    String sendMessage(SQSEvent event);
}
