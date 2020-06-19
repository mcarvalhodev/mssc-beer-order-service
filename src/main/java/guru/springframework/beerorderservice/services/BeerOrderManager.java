package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.brewery.model.AllocateOrderResponse;
import guru.springframework.beerorderservice.brewery.model.events.ValidateOrderResponse;
import guru.springframework.beerorderservice.domain.BeerOrder;

import java.util.function.Consumer;

public interface BeerOrderManager {

  BeerOrder create(BeerOrder order);

  void handle(ValidateOrderResponse response);

  Consumer<AllocateOrderResponse> onAllocationResult();
}
