package guru.springframework.beerorderservice.services.listeners;

import guru.springframework.beerorderservice.application.BeerOrderManager;
import guru.springframework.beerorderservice.domain.event.ValidateOrderResponse;
import guru.springframework.beerorderservice.infrastructure.jms.JmsConfig;
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
