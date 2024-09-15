package com.benhession.imagepicker.common.model;

import lombok.Builder;

@Builder
public record PageInfo(long numberItems, int page, int lastPage, int size) {
}
