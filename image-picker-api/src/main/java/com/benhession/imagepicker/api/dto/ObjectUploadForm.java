package com.benhession.imagepicker.api.dto;

import com.benhession.imagepicker.api.validation.EnumValidator;
import com.benhession.imagepicker.common.model.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjectUploadForm {

    @RestForm("data")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @NotNull
    private byte[] data;

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
