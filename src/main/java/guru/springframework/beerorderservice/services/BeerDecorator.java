package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.brewery.model.BeerOrderDto;
import guru.springframework.beerorderservice.domain.BeerOrder;
import guru.springframework.beerorderservice.services.beer.BeerOrderEnhancer;
import guru.springframework.beerorderservice.web.mappers.BeerOrderMapper;
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
