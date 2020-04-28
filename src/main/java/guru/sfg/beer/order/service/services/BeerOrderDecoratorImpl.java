package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
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
