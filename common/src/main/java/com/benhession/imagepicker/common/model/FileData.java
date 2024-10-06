package com.benhession.imagepicker.common.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.File;
import java.io.Serializable;

@RegisterForReflection
public record FileData(File data, String filename, String mimeType, String imageType) implements Serializable {
}
