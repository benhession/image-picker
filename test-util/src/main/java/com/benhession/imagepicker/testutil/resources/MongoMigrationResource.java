package com.benhession.imagepicker.testutil.resources;

import com.benhession.imagepicker.testutil.exception.TestDatabaseException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.testcontainers.mongodb.MongoDBAtlasLocalContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoMigrationResource implements QuarkusTestResourceLifecycleManager {

    private MongoDBAtlasLocalContainer mongoDbContainer;

    @Override
    public Map<String, String> start() {
        try {
            mongoDbContainer =
                new MongoDBAtlasLocalContainer(DockerImageName.parse("mongodb/mongodb-atlas-local:7.0.15"))
                    .withReuse(true)
                    .withExposedPorts(27017);
            mongoDbContainer.start();

            String databaseName = "test-db";
            String connectionString = mongoDbContainer.getConnectionString();

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
                throw new TestDatabaseException("MongoDB Migration failed with exit code " + exitCode);
            }

            checkSearchIndexesAreReady(connectionString, databaseName, Duration.ofSeconds(40));

            return Map.of(
                "quarkus.mongodb.devservices.enabled", "false",
                "quarkus.mongodb.database", databaseName,
                "quarkus.mongodb.connection-string", connectionString);

        } catch (IOException | InterruptedException e) {
            throw new TestDatabaseException(e);
        }

    }

    @Override
    public void stop() {
        if (mongoDbContainer != null) {
            mongoDbContainer.stop();
        }
    }

    private void checkSearchIndexesAreReady(String connectionString, String databaseName, Duration timeout) {
        var startTime = Instant.now();
        var endTime = startTime.plus(timeout);

        while (Instant.now().isBefore(endTime)) {
            if (searchIndexesAreReady(connectionString, databaseName)) {
                return;
            }
        }

        throw new TestDatabaseException("MongoDB Search indexes are not ready after " + timeout);
    }

    private boolean searchIndexesAreReady(String connectionString, String databaseName) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            List<Document> results = new ArrayList<>();

            try (var collections = db.listCollectionNames().iterator()) {
                collections.forEachRemaining(collectionName -> {
                    var collection = db.getCollection(collectionName);
                    try (var cursor = collection.aggregate(List.of(
                        new Document("$listSearchIndexes", new Document())
                    )).cursor()) {
                        cursor.forEachRemaining(results::add);
                    }
                });
            }

            if (results.isEmpty()) {
                return true;
            }

            return results.stream()
                .map(document -> document.get("status"))
                .allMatch(status -> status.equals("READY"));
        }
    }
}
