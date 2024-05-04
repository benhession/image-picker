package com.benhession.imagepicker.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "image")
public interface ImageConfigProperties {
    List<String> acceptedMimeTypes();
    ImageType size();

    interface ImageType {
        ImageSize square();

        ImageSize panoramic();

        ImageSize rectangular();

        ImageSize landscape();

        interface ImageSize {
            Scale thumbnail();

            Scale small();

            Scale medium();

            Scale large();

            float aspectRatio();
            int minWidth();

            interface Scale {
                float scalingFactor();
            }
        }
    }
}