package com.benhession.imagepicker.testutil;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

@ApplicationScoped
public class TestFileLoader {
    public File loadTestFile(String filename) {
        URL url = getClass().getClassLoader().getResource(filename);
        assert url != null;
        return new File(url.getFile());
    }

    public byte[] loadTestFileBytes(String filename) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(loadTestFile(filename))) {
            return fileInputStream.readAllBytes();
        }
    }
}
