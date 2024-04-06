package com.benhession.imagepicker.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;

@ApplicationScoped
public class ImageCreationService {
    public void createNewImagesFrom(File data) {
        // TODO:
        //  1. check image dimensions
        //  2. split into parts
        //  3. upload parts to s3
        //  4. update metadata in db
        //  5. return image id
    }
}
