package com.benhession.imagepicker.common.model;

import java.io.File;

public record FileData(File data, String filename, String mimeType, String imageType) {
}
