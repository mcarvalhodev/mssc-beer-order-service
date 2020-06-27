package guru.springframework.beerorderservice.services.testcomponents;

import guru.springframework.beerorderservice.domain.event.AllocateOrderRequest;
import guru.springframework.beerorderservice.domain.event.AllocateOrderResponse;
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
public class BeerOrderAllocationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
  public void listen(Message message) {

    AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();

    request.getOrder().getBeerOrderLines().stream()
        .forEach(
            beerOrderLineDto -> {
              beerOrderLineDto.setOrderQuantity(beerOrderLineDto.getOrderQuantity());
            });

    jmsTemplate.convertAndSend(
        JmsConfig.ALLOCATE_ORDER_RESPONSE,
        AllocateOrderResponse.builder()
            .order(request.getOrder())
            .pendingInventory(false)
            .allocationError(false)
            .build());
  }
}
