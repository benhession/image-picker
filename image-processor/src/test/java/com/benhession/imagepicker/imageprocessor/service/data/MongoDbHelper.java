package com.benhession.imagepicker.imageprocessor.service.data;

import com.benhession.imagepicker.data.model.ImageMetadata;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

@UtilityClass
public class MongoDbHelper {
    private static final String DATABASE_NAME = "image-picker";
    private static final CodecRegistry CODEC_REGISTRY = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()),
        CodecRegistries.fromCodecs(new ImageMetaDataCodec())
    );
    private static final MongoClient MONGO_CLIENT = MongoClients.create(
        MongoClientSettings.builder()
            .codecRegistry(CODEC_REGISTRY)
            .applyConnectionString(new ConnectionString("mongodb://localhost:56000"))
            .build()
    );
    private static final String IMAGES_COLLECTION = "images";

    public static ObjectId addImageMetaData(ImageMetadata imageMetadata) {
        var applicationDataBase = MONGO_CLIENT.getDatabase(DATABASE_NAME);
        var imagesCollection = applicationDataBase.getCollection(IMAGES_COLLECTION, ImageMetadata.class);

        imagesCollection.insertOne(imageMetadata);
        var savedMetadata = imagesCollection
            .find(new Document().append("parentKey", imageMetadata.getParentKey()), ImageMetadata.class)
            .first();
        if (savedMetadata == null) {
            throw new RuntimeException();
        }

        return savedMetadata.getId();
    }

    public static Optional<ImageMetadata> getImageMetaData(ObjectId imageId) {
        var applicationDataBase = MONGO_CLIENT.getDatabase(DATABASE_NAME);
        var imagesCollection = applicationDataBase.getCollection(IMAGES_COLLECTION, ImageMetadata.class);
        return Optional.ofNullable(imagesCollection.find(new Document("_id", imageId), ImageMetadata.class)
            .first());
    }
}
