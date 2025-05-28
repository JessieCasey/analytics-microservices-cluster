package io.javatab.microservices.core.app.prices.mapper;


import io.javatab.microservices.core.app.common.EntityDtoMapper;
import io.javatab.microservices.core.app.prices.dto.PriceDto;
import io.javatab.microservices.core.app.prices.model.Price;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PriceMapper extends EntityDtoMapper<Price, PriceDto> {

    PriceDto toDto(Price price);

    List<PriceDto> toDtoList(List<Price> prices);

}
