package com.benhession.imagepicker.common.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class MessageReaderWriterProvider {

    @Produces
    @Named("imageCreationMessageReader")
    ObjectReader imageCreationMessageReader() {
        return new ObjectMapper().readerFor(ImageCreationMessage.class);
    }

    @Produces
    @Named("imageCreationMessageWriter")
    ObjectWriter imageCreationMessageWriter() {
        return new ObjectMapper().writerFor(ImageCreationMessage.class);
    }
}
