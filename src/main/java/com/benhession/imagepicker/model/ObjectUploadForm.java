package com.benhession.imagepicker.model;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.File;

@Getter
@Setter
public class ObjectUploadForm {
    @FormParam("data")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    private File data;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    private String filename;

    @FormParam("mimetype")
    @PartType(MediaType.TEXT_PLAIN)
    private String mimetype;
}
