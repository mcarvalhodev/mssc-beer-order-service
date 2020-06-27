package guru.springframework.beerorderservice.services.listeners;

import guru.springframework.beerorderservice.application.BeerOrderManager;
import guru.springframework.beerorderservice.domain.event.AllocateOrderResponse;
import guru.springframework.beerorderservice.infrastructure.jms.JmsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllocationResultListener implements Listener<AllocateOrderResponse> {

  private final BeerOrderManager orderManager;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE)
  @Override
  public void listen(AllocateOrderResponse payload) {
    orderManager.onAllocationResult().accept(payload);
  }
}
