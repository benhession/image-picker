package com.benhession.imagepicker.common.security;

import io.quarkus.security.identity.SecurityIdentity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class UserInfo {

    public static final String EDITOR_ROLE = "admin";

    private final SecurityIdentity securityIdentity;

    public String getUserName() {
        return securityIdentity.getPrincipal().getName();
    }

    public boolean isEditor() {
        return securityIdentity != null && securityIdentity.hasRole(EDITOR_ROLE);
    }
}
