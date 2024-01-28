package com.benhession.imagepicker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ImageHeightWidth {
    private int height;
    private int width;
}
