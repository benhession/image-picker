package com.benhession.imagepicker.data.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PreSignedUploadDto {
    private String url;
    private Map<String, String> headers;
}
