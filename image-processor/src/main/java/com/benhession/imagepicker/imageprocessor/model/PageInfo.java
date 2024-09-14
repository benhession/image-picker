package com.benhession.imagepicker.imageprocessor.model;

import lombok.Builder;

@Builder
public record PageInfo(long numberItems, int page, int lastPage, int size) {
}
