package com.benhession.imagepicker.imageprocessor.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
public class ClassifierLambdaConfig {

    @ConfigProperty(name = "lambda.classifier.function-name")
    String functionName;
}
