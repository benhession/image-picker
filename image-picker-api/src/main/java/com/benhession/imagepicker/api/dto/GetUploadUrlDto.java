package com.benhession.imagepicker.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUploadUrlDto {

    @NotBlank
    private String filename;

    @NotBlank
    private String mimetype;

    private List<String> tags;

}
