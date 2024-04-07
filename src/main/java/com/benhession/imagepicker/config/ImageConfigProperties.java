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
            ImageHeightWidth thumbnail();

            ImageHeightWidth small();

            ImageHeightWidth medium();

            ImageHeightWidth large();

            interface ImageHeightWidth {
                int width();

                int height();
            }
        }
    }
}