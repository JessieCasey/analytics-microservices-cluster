package io.javatab.microservices.core.app.common;

public interface EntityDtoMapper<E,D> {
    D toDto(E entity);
}
