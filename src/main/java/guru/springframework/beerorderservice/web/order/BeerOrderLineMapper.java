package guru.springframework.beerorderservice.web.order;

import guru.springframework.beerorderservice.domain.model.order.BeerOrderLine;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderLineDto;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface BeerOrderLineMapper {
  BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);

  BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}
