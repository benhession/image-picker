package com.benhession.imagepicker.api.model;

import lombok.Builder;

@Builder
public record PageInfo(long numberItems, int page, int lastPage, int size) {
}
