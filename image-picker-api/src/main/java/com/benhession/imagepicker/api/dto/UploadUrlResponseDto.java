package com.benhession.imagepicker.api.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UploadUrlResponseDto {
    private String imageId;
    private String uploadUrl;
}
