package com.benhession.imagepicker.imageprocessor.service;

import com.benhession.imagepicker.common.exception.ImageClassifierException;
import com.benhession.imagepicker.imageprocessor.config.ClassifierLambdaConfig;
import com.benhession.imagepicker.imageprocessor.model.ClassifierResult;
import com.benhession.imagepicker.imageprocessor.model.ImageClassifierRequest;
import com.benhession.imagepicker.imageprocessor.model.ImageClassifierRequest.Body;
import com.benhession.imagepicker.imageprocessor.model.ImageClassifierResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.MDC;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.ServiceException;


@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ImageClassifierService {

    private static final double MATCH_THRESHOLD = 0.8;

    private final ManagedExecutor managedExecutor;
    private final LambdaClient lambdaClient;
    private final ClassifierLambdaConfig classifierLambdaConfig;
    private final ObjectMapper objectMapper;

    public Future<List<String>> findClassifiersAsync(String imageKey) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return managedExecutor.supplyAsync(
                () -> {
                    if (mdcContext != null) {
                        MDC.setContextMap(mdcContext);
                    }
                    try {
                        return findClassifiers(imageKey);
                    } finally {
                        MDC.clear();
                    }
                })
            .exceptionally(e -> {
                log.error("Error getting classifiers for image key: {}", imageKey, e);
                return List.of();
            });
    }

    private List<String> findClassifiers(String imageKey) throws ImageClassifierException {
        try {
            var imageClassifierRequest =
                ImageClassifierRequest.of(Body.builder().objectKey(imageKey).build(), objectMapper);
            InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(classifierLambdaConfig.getFunctionName())
                .payload(SdkBytes.fromByteArray(imageClassifierRequest.toJsonByteArray(objectMapper)))
                .build();

            var invokeResponse = lambdaClient.invoke(invokeRequest);

            if (invokeResponse.functionError() != null && !invokeResponse.functionError().isBlank()) {
                throw new ImageClassifierException(
                    "Lambda returned function error: " + invokeResponse.functionError() + ":\n"
                        + invokeResponse.payload().asString(StandardCharsets.UTF_8));
            }

            ImageClassifierResponse imageClassifierResponse =
                ImageClassifierResponse.fromJsonBytes(invokeResponse.payload().asByteArray(), objectMapper);

            if (imageClassifierResponse.getStatusCode() == null
                || !imageClassifierResponse.getStatusCode().equals("200")) {
                throw new ImageClassifierException(
                    "Unexpected response status: " + imageClassifierResponse.getStatusCode());
            }

            List<ClassifierResult> classifierResults =
                objectMapper.readValue(imageClassifierResponse.getBody(), new TypeReference<>() {
                });

            return classifierResults.stream()
                .filter(result -> result.getScore() >= MATCH_THRESHOLD)
                .flatMap(this::joinCategoriesAndLabel)
                .toList();

        } catch (JsonProcessingException e) {
            throw new ImageClassifierException("Failed build image classifier request", e);
        } catch (ServiceException e) {
            throw new ImageClassifierException("Failed to call image classifier service", e);
        }
    }

    private Stream<String> joinCategoriesAndLabel(ClassifierResult result) {
        var joinedCategories = new ArrayList<>(result.getCategories());
        joinedCategories.add(result.getLabel());
        return joinedCategories.stream();
    }
}
