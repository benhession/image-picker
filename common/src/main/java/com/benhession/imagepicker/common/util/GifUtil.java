package com.benhession.imagepicker.common.util;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;

@ApplicationScoped
public class GifUtil {

    public byte[] alterGifFrames(byte[] data, Function<BufferedImage, BufferedImage> perFrameFunction)
        throws IOException {

        try (InputStream inputStream = new ByteArrayInputStream(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            List<Frame> frames = new ArrayList<>();
            var gifDecoder = new GifDecoder();

            gifDecoder.read(inputStream);

            for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
                Frame newFrame = Frame.builder()
                    .image(perFrameFunction.apply(gifDecoder.getFrame(i)))
                    .delay(gifDecoder.getDelay(i))
                    .build();
                frames.add(newFrame);
            }

            var gifEncoder = new AnimatedGifEncoder();
            gifEncoder.start(outputStream);
            gifEncoder.setRepeat(gifDecoder.getLoopCount());
            gifEncoder.setDelay(calculateAverageDelay(frames));

            for (var frame : frames) {
                gifEncoder.addFrame(frame.image);
            }
            gifEncoder.finish();

            return outputStream.toByteArray();
        }
    }

    private int calculateAverageDelay(List<Frame> frames) {
        var averageDelay = frames.stream()
            .map(Frame::delay)
            .mapToInt(a -> a)
            .summaryStatistics()
            .getAverage();
        return Math.toIntExact(Math.round(averageDelay));
    }

    @Builder
    private record Frame(BufferedImage image, int delay) {

    }
}
