package com.benhession.imagepicker.common.util;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jodd.net.MimeTypes;

@ApplicationScoped
public class MimeTypeUtil {
    public String mimeTypeToFileFormat(String mimeType) {
        String [] extensions = MimeTypes.findExtensionsByMimeTypes(mimeType, false);

        if (extensions.length > 0) {
            return extensions[0];
        }

        throw new ImageProcessingException("Unable to obtain file format for mime type: " + mimeType);
    }
}
