package guru.springframework.beerorderservice.services.testcomponents;

import guru.springframework.beerorderservice.domain.event.ValidateOrderRequest;
import guru.springframework.beerorderservice.domain.event.ValidateOrderResponse;
import guru.springframework.beerorderservice.infrastructure.jms.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
  public void listen(Message<ValidateOrderRequest> message) {

    final ValidateOrderRequest payload = message.getPayload();

    boolean sendResponse = true;

    boolean isValid = true;

    if (payload.getOrder().getCustomerRef() != null) {
      if (payload.getOrder().getCustomerRef().equals("fail-validation")) {
        isValid = false;
      } else if (payload.getOrder().getCustomerRef().equals("dont-validate")) {
        sendResponse = false;
      }
    }

    if (sendResponse) {
      jmsTemplate.convertAndSend(
          JmsConfig.VALIDATE_ORDER_RESPONSE,
          ValidateOrderResponse.builder()
              .valid(isValid)
              .orderId(payload.getOrder().getId())
              .build());
    }
  }
}
