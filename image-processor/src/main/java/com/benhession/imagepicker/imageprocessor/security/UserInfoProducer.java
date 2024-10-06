package com.benhession.imagepicker.imageprocessor.security;

import static com.benhession.imagepicker.imageprocessor.security.UserInfo.USERNAME_ATTRIBUTE_KEY;

import com.benhession.imagepicker.common.exception.SecurityException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
@RequiredArgsConstructor
public class UserInfoProducer {

    private final JWTParser jwtParser;
    private SecurityIdentity securityIdentity;

    public UserInfo init(String token) {
        securityIdentity = parse(token);
        return UserInfo.of(securityIdentity);
    }

    @Produces
    @RequestScoped
    @Priority(Integer.MAX_VALUE)
    public UserInfo getUserInfo() {
        return UserInfo.of(securityIdentity);
    }

    private SecurityIdentity parse(String token) throws SecurityException {
        JsonWebToken jwt;
        try {
            jwt = jwtParser.parse(token);
        } catch (ParseException e) {
            throw new SecurityException(e.getMessage());
        }

        var builder = QuarkusSecurityIdentity.builder()
          .addAttribute(USERNAME_ATTRIBUTE_KEY, jwt.getClaim("sub"))
          .addRoles(jwt.getGroups());

        return builder.build();
    }
}
