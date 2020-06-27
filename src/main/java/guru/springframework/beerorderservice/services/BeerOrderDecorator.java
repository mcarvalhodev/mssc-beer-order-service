package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.domain.model.order.BeerOrder;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderDto;

public interface BeerOrderDecorator {
    BeerOrderDto beerOrderToDto(BeerOrder beerOrder);

    BeerOrder dtoToBeerOrder(BeerOrderDto dto);
}
