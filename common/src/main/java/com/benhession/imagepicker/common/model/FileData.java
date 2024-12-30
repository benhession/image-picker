package com.benhession.imagepicker.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    private byte[] data;
    private String filename;
    private String mimeType;
    private String imageType;
}
