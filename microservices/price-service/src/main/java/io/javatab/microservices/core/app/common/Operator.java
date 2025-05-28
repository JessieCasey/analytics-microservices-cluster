package io.javatab.microservices.core.app.common;

import java.util.Arrays;

public enum Operator {
    eq, ne, gt, gte, lt, lte;

    public static Operator of(String token) {
        return Arrays.stream(values())
                .filter(o -> o.name().equalsIgnoreCase(token))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + token));
    }
}

