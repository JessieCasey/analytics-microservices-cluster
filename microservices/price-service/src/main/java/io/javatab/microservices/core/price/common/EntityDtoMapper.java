package io.javatab.microservices.core.price.common;

public interface EntityDtoMapper<E,D> {
    D toDto(E entity);
}
