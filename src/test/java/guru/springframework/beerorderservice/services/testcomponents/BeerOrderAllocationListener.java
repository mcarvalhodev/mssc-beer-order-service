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
  public void listen(Message<AllocateOrderRequest> message) {

    AllocateOrderRequest request = message.getPayload();
    boolean pendingInventory = false;
    boolean allocationError = false;
    boolean sendResponse = true;

    if (request.getOrder().getCustomerRef() != null) {
      if (request.getOrder().getCustomerRef().equals("fail-allocation")) {
        allocationError = true;
      }
      if (request.getOrder().getCustomerRef().equals("dont-allocate")) {
        sendResponse = false;
      }
      if (request.getOrder().getCustomerRef().equals("partial-allocation")) {
        pendingInventory = true;
      }
    }

    boolean finalPendingInventory = pendingInventory;
    request
        .getOrder()
        .getBeerOrderLines()
        .forEach(
            beerOrderLineDto -> {
              if (finalPendingInventory) {
                beerOrderLineDto.setOrderQuantity(beerOrderLineDto.getOrderQuantity() - 1);
              } else {
                beerOrderLineDto.setOrderQuantity(beerOrderLineDto.getOrderQuantity());
              }
            });

    if (sendResponse) {
      jmsTemplate.convertAndSend(
          JmsConfig.ALLOCATE_ORDER_RESPONSE,
          AllocateOrderResponse.builder()
              .order(request.getOrder())
              .pendingInventory(pendingInventory)
              .allocationError(allocationError)
              .build());
    }
  }
}
