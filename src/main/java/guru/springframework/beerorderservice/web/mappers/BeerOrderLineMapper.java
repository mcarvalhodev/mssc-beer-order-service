package guru.springframework.beerorderservice.web.mappers;

import guru.springframework.beerorderservice.brewery.model.BeerOrderLineDto;
import guru.springframework.beerorderservice.domain.BeerOrderLine;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface BeerOrderLineMapper {
  BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);

  BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}
