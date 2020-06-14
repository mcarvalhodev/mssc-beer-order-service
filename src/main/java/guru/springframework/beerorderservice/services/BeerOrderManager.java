package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.domain.BeerOrder;

public interface BeerOrderManager {

  BeerOrder create(BeerOrder order);
}
