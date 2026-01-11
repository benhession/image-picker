package com.benhession.imagepicker.api.controller;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import com.benhession.imagepicker.api.dto.ImageAttributesDto;
import com.benhession.imagepicker.api.mapper.ImageAttributesMapper;
import com.benhession.imagepicker.common.config.ImageConfigProperties;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/attributes")
@RequiredArgsConstructor
public class AttributesController {

    private final ImageAttributesMapper imageAttributesMapper;
    private final ImageConfigProperties imageConfigProperties;

    @GET
    @RolesAllowed({"admin"})
    @Produces(APPLICATION_JSON)
    public RestResponse<ImageAttributesDto> getAttributes() {
        return RestResponse.ok(imageAttributesMapper.toDto(imageConfigProperties));
    }
}
