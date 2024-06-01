package com.benhession.imagepicker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;

import com.benhession.imagepicker.dto.ImageUploadDto;
import com.benhession.imagepicker.exception.ImageProcessingException;
import com.benhession.imagepicker.testutil.TestFileLoader;
import com.benhession.imagepicker.util.MimeTypeUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@QuarkusTest
public class S3StorageServiceTest {

    private static final String TEST_MIME_TYPE = "image/jpeg";
    private static final String TEST_ORIGINAL_FILENAME = "test-filename.jpg";
    @Inject
    MimeTypeUtil mimeTypeUtil;
    @Inject
    S3StorageService s3StorageService;
    @InjectSpy
    S3Client s3Client;
    @Inject
    TestFileLoader testFileLoader;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    private String testFilename;
    private String testFilename2;

    @BeforeEach
    public void setup() {
        testFilename = generateTestFilename();
        testFilename2 = generateTestFilename();
    }

    @AfterEach
    public void cleanup() {
        clearS3();
    }

    @Test
    public void When_UploadFiles_With_NoError_Expect_ImagesUploaded() throws IOException {

        s3StorageService.uploadFiles(TEST_ORIGINAL_FILENAME, getTestDto());

        var listObjectsV2Response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
          .bucket(bucketName)
          .build());

        List<String> keys = listObjectsV2Response.contents()
          .stream()
          .map(S3Object::key)
          .filter(key -> key.contains(TEST_ORIGINAL_FILENAME))
          .toList();

        assertThat(keys.size()).isEqualTo(2);

        Optional<String> file1Key = keys.stream()
          .filter(key -> key.contains(testFilename))
          .findFirst();

        assertThat(file1Key.isPresent()).isTrue();

        Optional<String> file2Key = keys.stream()
          .filter(key -> key.contains(testFilename2))
          .findFirst();

        assertThat(file2Key.isPresent()).isTrue();

    }

    @Test
    public void When_UploadFiles_With_AwsServiceException_Expect_NoImagesUploaded() {

        doCallRealMethod()
          .doThrow(AwsServiceException.class)
          .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThatThrownBy(() -> s3StorageService.uploadFiles(TEST_ORIGINAL_FILENAME, getTestDto()))
          .isInstanceOf(ImageProcessingException.class)
          .hasMessageContaining("Error uploading file to S3 for filename: " + testFilename2);

        var listObjectsV2Response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
          .bucket(bucketName)
          .build());

        assertThat(listObjectsV2Response.contents().isEmpty()).isTrue();

    }

    private List<ImageUploadDto> getTestDto() throws IOException {
        BufferedImage image1 = getBufferedImageForFile("test.jpeg");
        BufferedImage image2 = getBufferedImageForFile("test-image.jpg");

        return List.of(
          new ImageUploadDto(testFilename, TEST_MIME_TYPE, bufferedImageToByteArray(image1)),
          new ImageUploadDto(testFilename2, TEST_MIME_TYPE, bufferedImageToByteArray(image2))
        );
    }

    private byte[] bufferedImageToByteArray(BufferedImage bufferedImage) throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, mimeTypeUtil.mimeTypeToFileFormat(TEST_MIME_TYPE), outputStream);

            return outputStream.toByteArray();
        }
    }

    private BufferedImage getBufferedImageForFile(String filename) throws IOException {
        return ImageIO.read(testFileLoader.loadTestFile(filename));
    }

    private String generateTestFilename() {
        return "test-file-" + UUID.randomUUID() + ".jpg";
    }

    private void clearS3() {
        var listObjectsV2Response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
          .bucket(bucketName)
          .build());

        for (S3Object s3Object : listObjectsV2Response.contents()) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
              .bucket(bucketName)
              .key(s3Object.key())
              .build());
        }
    }
}
