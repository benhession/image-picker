package com.benhession.imagepicker.imageprocessor.service;

import com.benhession.imagepicker.common.exception.ImageProcessingException;
import com.benhession.imagepicker.common.model.FileData;
import com.benhession.imagepicker.data.service.ObjectStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class FileDataRetrievalService {
    private final ObjectStorageService objectStorageService;
    private final Logger logger;

    public Optional<FileData> retrieve(String fileDataKey) {
        try {
            return Optional.of(objectStorageService.getOriginalFileData(fileDataKey));
        } catch (ImageProcessingException e) {
            logger.error("Error while retrieving original file data", e);
            return Optional.empty();
        }
    }
}
