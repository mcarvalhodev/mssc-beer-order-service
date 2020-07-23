package guru.springframework.beerorderservice.application;

import guru.springframework.beerorderservice.domain.model.order.BeerOrder;

import java.util.function.Consumer;

public class CancelOrderEvent implements Consumer<BeerOrder> {
  @Override
  public void accept(BeerOrder order) {}
}
