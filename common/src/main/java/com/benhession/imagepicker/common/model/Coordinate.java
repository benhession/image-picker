package com.benhession.imagepicker.common.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Coordinate(int x, int y) {

}
