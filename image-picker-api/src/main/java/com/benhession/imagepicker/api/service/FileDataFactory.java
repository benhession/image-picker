package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.api.dto.ObjectUploadForm;
import com.benhession.imagepicker.common.model.FileData;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileDataFactory {
    public FileData fromObjectUploadForm(ObjectUploadForm objectUploadForm) {
        return new FileData(
            objectUploadForm.getData(),
            objectUploadForm.getFilename(),
            objectUploadForm.getMimetype(),
            objectUploadForm.getImageType()
        );
    }
}
