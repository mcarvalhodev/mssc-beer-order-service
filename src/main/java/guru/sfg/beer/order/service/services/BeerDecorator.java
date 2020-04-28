package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.services.beer.BeerOrderEnhancer;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import org.springframework.stereotype.Component;

@Component
public class BeerDecorator extends BeerOrderDecoratorImpl {

  private final BeerOrderEnhancer enhancer;

  public BeerDecorator(BeerOrderMapper mapper, BeerOrderEnhancer enhancer) {
    super(mapper);
    this.enhancer = enhancer;
  }

  @Override
  public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
    return enhancer.enhance(super.beerOrderToDto(beerOrder));
  }

  @Override
  public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
    return super.dtoToBeerOrder(dto);
  }
}
