package guru.springframework.beerorderservice.application;

import guru.springframework.beerorderservice.domain.event.AllocateOrderResponse;
import guru.springframework.beerorderservice.domain.event.ValidateOrderResponse;
import guru.springframework.beerorderservice.domain.model.order.BeerOrder;

import java.util.UUID;
import java.util.function.Consumer;

public interface BeerOrderManager {

  BeerOrder create(BeerOrder order);

  void handle(ValidateOrderResponse response);

  Consumer<AllocateOrderResponse> onAllocationResult();

  void beerOrderPickedUp(UUID id);
}
