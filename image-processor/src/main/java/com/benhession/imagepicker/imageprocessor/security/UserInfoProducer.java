package com.benhession.imagepicker.imageprocessor.security;

import com.benhession.imagepicker.common.exception.SecurityException;
import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.TenantIdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@RequestScoped
@RequiredArgsConstructor
public class UserInfoProducer {
    private final TenantIdentityProvider defaultIdentityProvider;
    private final Logger logger;

    private UserInfo userInfo;

    public UserInfo init(String token) {
        userInfo = UserInfo.of(parse(token));
        return userInfo;
    }

    @Produces
    @RequestScoped
    @Priority(Integer.MAX_VALUE)
    public UserInfo getUserInfo() {
        return userInfo;
    }

    private SecurityIdentity parse(String token) throws SecurityException {
        try {
            AccessTokenCredential tokenCredential = new AccessTokenCredential(token);
            return defaultIdentityProvider.authenticate(tokenCredential).await()
                .atMost(Duration.ofSeconds(10));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SecurityException(e.getMessage());
        }
    }
}
