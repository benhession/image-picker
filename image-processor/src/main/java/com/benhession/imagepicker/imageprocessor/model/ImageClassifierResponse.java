package com.benhession.imagepicker.imageprocessor.model;

import com.benhession.imagepicker.common.exception.ImageClassifierException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RegisterForReflection
public class ImageClassifierResponse {

    private String statusCode;
    private Headers headers;
    private boolean isBase64Encoded;
    private String body;

    @Getter
    @Setter
    @ToString
    @RegisterForReflection
    public static class Headers {

        @JsonProperty("Content-Type")
        private String contentType;
        @JsonProperty("Content-Length")
        private String contentLength;
    }

    public static ImageClassifierResponse fromJsonBytes(byte[] bytes, ObjectMapper objectMapper) throws
        ImageClassifierException {
        try {
            return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), ImageClassifierResponse.class);
        } catch (IOException e) {
            throw new ImageClassifierException("Error parsing ImageClassifierResponse", e);
        }

    }
}
