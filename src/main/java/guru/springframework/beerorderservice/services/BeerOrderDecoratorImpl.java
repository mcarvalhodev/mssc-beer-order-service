package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.domain.model.order.BeerOrder;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderDto;
import guru.springframework.beerorderservice.web.order.BeerOrderMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BeerOrderDecoratorImpl implements BeerOrderDecorator {

  private final BeerOrderMapper mapper;

  @Override
  public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
    return mapper.beerOrderToDto(beerOrder);
  }

  @Override
  public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
    return mapper.dtoToBeerOrder(dto);
  }
}
