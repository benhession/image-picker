package com.benhession.imagepicker.common.config;

import io.smallrye.config.ConfigMapping;
import java.time.Duration;

@ConfigMapping(prefix = "image-processing-properties")
public interface ImageProcessingProperties {
    Duration timeout();
}
