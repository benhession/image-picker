package com.benhession.imagepicker.dto;

import com.benhession.imagepicker.model.ImageType;
import com.benhession.imagepicker.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;

@Getter
@Setter
public class ObjectUploadForm {

    @RestForm("data")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private File data;

    @RestForm("filename")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String filename;

    @RestForm("mime-type")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String mimetype;

    @RestForm("image-type")
    @EnumValidator(enumClass = ImageType.class)
    private String imageType;
}
