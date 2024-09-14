package com.benhession.imagepicker.api.testutil;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.net.URL;

@ApplicationScoped
public class TestFileLoader {
    public File loadTestFile(String filename) {
        URL url = getClass().getClassLoader().getResource(filename);
        assert url != null;
        return new File(url.getFile());
    }
}
