package com.benhession.imagepicker.imageprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@RegisterForReflection
public class ImageClassifierRequest {

    private final String httpMethod = "POST";
    private final String path = "/";
    private final Map<String, String> queryStringParameters = Map.of();
    private final Headers headers = new Headers();
    private final String body;

    public static ImageClassifierRequest of(Body body, ObjectMapper objectMapper) throws JsonProcessingException {
        return new ImageClassifierRequest(body, objectMapper);
    }

    private ImageClassifierRequest(Body body, ObjectMapper objectMapper) throws JsonProcessingException {
        this.body = objectMapper.writeValueAsString(body);
    }

    @Getter
    @ToString
    @RegisterForReflection
    public static class Headers {

        @JsonProperty("Content-Type")
        private final String contentType = "application/json";
    }

    @Builder
    @Getter
    @ToString
    @RegisterForReflection
    public static class Body {

        private final String objectKey;
    }

    public byte[] toJsonByteArray(ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsString(this).getBytes(StandardCharsets.UTF_8);
    }
}
