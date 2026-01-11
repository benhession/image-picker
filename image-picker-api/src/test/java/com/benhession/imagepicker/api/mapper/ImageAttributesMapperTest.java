package com.benhession.imagepicker.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.benhession.imagepicker.api.dto.ImageAttributesDto;
import com.benhession.imagepicker.common.config.ImageConfigProperties;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ImageAttributesMapperTest {

    @Inject
    ImageAttributesMapper imageAttributesMapper;
    @Inject
    ImageConfigProperties imageConfigProperties;

    @Test
    public void When_ToDto_Expect_ValuesMapped() {
        // act
        ImageAttributesDto result = imageAttributesMapper.toDto(imageConfigProperties);

        // assert
        assertThat(result.getAcceptedMimeTypes()).isEqualTo(List.of("image/jpeg", "image/png", "image/gif"));
        assertThat(result.getSquare()).isNotNull();
        assertThat(result.getSquare().getAspectRatio()).isEqualTo(1.0);
        assertThat(result.getSquare().getMinWidth()).isEqualTo(1200);
        assertThat(result.getSquare().getThumbnail().getScalingFactor()).isEqualTo(0.1);
        assertThat(result.getSquare().getThumbnail().getHeight()).isEqualTo(120);
        assertThat(result.getSquare().getThumbnail().getWidth()).isEqualTo(120);
    }
}
