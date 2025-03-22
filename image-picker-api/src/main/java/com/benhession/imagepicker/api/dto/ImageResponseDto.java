package com.benhession.imagepicker.api.dto;

import com.benhession.imagepicker.common.model.ImageSize;
import com.benhession.imagepicker.common.model.ImageType;
import com.benhession.imagepicker.data.model.ImageProcessingStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponseDto {

    private String id;
    private String filename;
    private ImageType type;
    private List<String> tags;
    private List<String> aiTags;
    private Map<ImageSize, String> images = new HashMap<>();
    private ImageProcessingStatus status;
}
