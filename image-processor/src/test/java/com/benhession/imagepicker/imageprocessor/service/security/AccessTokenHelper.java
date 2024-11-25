package com.benhession.imagepicker.imageprocessor.service.security;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccessTokenHelper {
    private static final String OIDC_SERVER_URL = System.getenv("AUTH_SERVER_URL");
    private static final String OIDC_CLIENT_ID = System.getenv("OIDC_CLIENT_ID");
    private static final String OIDC_CLIENT_SECRET = System.getenv("OIDC_CLIENT_SECRET");
    private static final String TEST_USER_USERNAME = System.getenv("OIDC_TEST_USER_USERNAME");
    private static final String TEST_USER_PASSWORD = System.getenv("OIDC_TEST_USER_PASSWORD");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String getToken() throws IOException, InterruptedException {
        var base64Encoder = Base64.getEncoder();
        try (HttpClient httpClient = HttpClient.newBuilder().build()) {
            var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(OIDC_SERVER_URL + "/v1/token"))
                .headers(
                    "content-type", "application/x-www-form-urlencoded",
                    "accept", "application/json",
                    "authorization", "Basic " + base64Encoder.encodeToString(String.format("%s:%s",
                        OIDC_CLIENT_ID, OIDC_CLIENT_SECRET).getBytes()))
                .POST(HttpRequest.BodyPublishers.ofString(
                    String.format("&grant_type=password&username=%s&password=%s&scope=openid",
                        URLEncoder.encode(TEST_USER_USERNAME, UTF_8), URLEncoder.encode(TEST_USER_PASSWORD, UTF_8))))
                .build();
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            var tokenResponse = OBJECT_MAPPER.readValue(httpResponse.body(), TokenResponse.class);
            return tokenResponse.getAccessToken();
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private class TokenResponse {
        @JsonAlias(value = "access_token")
        @SuppressWarnings("unused")
        private String accessToken;
    }
}
