package com.benhession.imagepicker.api.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UploadUrlResponseDto {
    private String imageId;
    private String uploadUrl;
    private Map<String, String> headers;
}
