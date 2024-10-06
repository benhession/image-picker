package com.benhession.imagepicker.imageprocessor.security;

import io.quarkus.security.identity.SecurityIdentity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class UserInfo {
    static final String USERNAME_ATTRIBUTE_KEY = "username";
    public static final String EDITOR_ROLE = "blog-admin";

    private final SecurityIdentity securityIdentity;

    public String getUserName() {
        return securityIdentity.getAttribute(USERNAME_ATTRIBUTE_KEY);
    }

    public boolean isEditor() {
        return securityIdentity.hasRole(EDITOR_ROLE);
    }
}
