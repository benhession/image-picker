package com.benhession.imagepicker.dto;

import com.benhession.imagepicker.model.ImageSize;
import com.benhession.imagepicker.model.ImageType;
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
    private Map<ImageSize, String> images;
}
