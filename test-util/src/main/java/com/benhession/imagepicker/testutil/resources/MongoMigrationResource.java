package com.benhession.imagepicker.testutil.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import org.eclipse.microprofile.config.ConfigProvider;

public class MongoMigrationResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        try {
            String databaseName = ConfigProvider.getConfig()
                .getValue("%test.quarkus.mongodb.database", String.class);
            String port = ConfigProvider.getConfig()
                .getValue("%test.quarkus.mongodb.devservices.port", String.class);
            String host = "localhost";

            String connectionString = "mongodb://" + host + ":" + port + "/?directConnection=true";

            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File("../mongo-db-migrations"));
            pb.environment().put("MONGO_URL", connectionString);
            pb.environment().put("DATABASE_NAME", databaseName);
            pb.redirectErrorStream(true);
            pb.command("npx", "migrate-mongo", "up");

            Process process = pb.start();
            int exitCode = process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Migration output: " + line);
                }
            }

            if (exitCode != 0) {
                throw new RuntimeException("MongoDB Migration failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Map.of();
    }

    @Override
    public void stop() {
        // no teardown needed
    }
}
