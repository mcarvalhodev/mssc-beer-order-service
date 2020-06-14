package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.brewery.model.BeerOrderDto;
import guru.springframework.beerorderservice.domain.BeerOrder;

public interface BeerOrderDecorator {
    BeerOrderDto beerOrderToDto(BeerOrder beerOrder);

    BeerOrder dtoToBeerOrder(BeerOrderDto dto);
}
