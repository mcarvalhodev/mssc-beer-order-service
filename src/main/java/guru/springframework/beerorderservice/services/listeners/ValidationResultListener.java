package guru.springframework.beerorderservice.services.listeners;

import guru.springframework.beerorderservice.brewery.model.events.ValidateOrderResponse;
import guru.springframework.beerorderservice.config.JmsConfig;
import guru.springframework.beerorderservice.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationResultListener implements Listener<ValidateOrderResponse> {

  private final BeerOrderManager orderManager;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE)
  @Override
  public void listen(ValidateOrderResponse payload) {
    final UUID orderId = payload.getOrderId();
    log.debug("Validation result for Order[id=" + orderId + "]");
    orderManager.handle(payload);
  }
}
