package com.benhession.imagepicker.api.dto;

import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ImageResponseDto {
    private String id;
    private String filename;
    private ImageType type;
    private List<String> tags;
    private Map<ImageSize, String> images = new HashMap<>();
    private ImageProcessingStatus status;
}
