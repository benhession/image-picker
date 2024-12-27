package com.benhession.imagepicker.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
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
public class GetUploadUrlDto {
    @RestForm("filename")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String filename;

    @RestForm("mime-type")
    @PartType(MediaType.TEXT_PLAIN)
    @NotBlank
    private String mimetype;

    @RestForm("tag")
    @PartType(MediaType.TEXT_PLAIN)
    private List<String> tags;


}
