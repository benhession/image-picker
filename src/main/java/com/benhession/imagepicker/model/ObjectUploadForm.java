package com.benhession.imagepicker.model;

import com.benhession.imagepicker.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private File data;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String filename;

    @FormParam("mime-type")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String mimetype;

    @FormParam("image-type")
    @EnumValidator(enumClass = ImageType.class)
    private String imageType;
}
