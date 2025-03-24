package com.benhession.imagepicker.testutil.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoMigrationResource implements QuarkusTestResourceLifecycleManager {

    private MongoDBContainer mongoDbContainer;

    @Override
    public Map<String, String> start() {
        try {
            mongoDbContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
                .withReuse(true)
                .withExposedPorts(27017);
            mongoDbContainer.start();

            String databaseName = "test-db";
            String host = mongoDbContainer.getHost();
            int port = mongoDbContainer.getFirstMappedPort();
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

            return Map.of(
                "quarkus.mongodb.devservices.enabled", "false",
                "quarkus.mongodb.database", databaseName,
                "quarkus.mongodb.connection-string", connectionString);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stop() {
        if (mongoDbContainer != null) {
            mongoDbContainer.stop();
        }
    }
}
