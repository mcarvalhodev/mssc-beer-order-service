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

import static org.springframework.util.StringUtils.hasLength;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
  public void listen(Message<ValidateOrderRequest> message) {

    final ValidateOrderRequest payload = message.getPayload();

    final boolean isValid =
        hasLength(payload.getOrder().getCustomerRef())
                && payload.getOrder().getCustomerRef().equals("fail-validation")
            ? false
            : true;

    jmsTemplate.convertAndSend(
        JmsConfig.VALIDATE_ORDER_RESPONSE,
        ValidateOrderResponse.builder().valid(isValid).orderId(payload.getOrder().getId()).build());
  }
}
