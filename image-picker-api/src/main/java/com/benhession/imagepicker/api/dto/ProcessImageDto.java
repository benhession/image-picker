package com.benhession.imagepicker.api.dto;

import com.benhession.imagepicker.api.validation.EnumValidator;
import com.benhession.imagepicker.common.model.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.resteasy.reactive.RestForm;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessImageDto {
    @RestForm("image-type")
    @EnumValidator(enumClass = ImageType.class)
    private String imageType;
}
