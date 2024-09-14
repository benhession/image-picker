package com.benhession.imagepicker.imageprocessor.dto;

import com.benhession.imagepicker.imageprocessor.model.ImageSize;
import com.benhession.imagepicker.imageprocessor.model.ImageType;
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
    private Map<ImageSize, String> images;
}
