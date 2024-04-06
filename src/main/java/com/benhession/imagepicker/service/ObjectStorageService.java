package com.benhession.imagepicker.service;

import com.benhession.imagepicker.model.ObjectUploadForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ApplicationScoped
public class ObjectStorageService {

    @Inject
    S3Client s3Client;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    public boolean upload(ObjectUploadForm formData) {
        PutObjectResponse response = s3Client.putObject(buildPutRequest(formData), RequestBody.fromFile(formData.getData()));
        return response != null;
    }

    private PutObjectRequest buildPutRequest(ObjectUploadForm formData) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(formData.getFilename())
                .contentType(formData.getMimetype())
                .build();
    }
}
