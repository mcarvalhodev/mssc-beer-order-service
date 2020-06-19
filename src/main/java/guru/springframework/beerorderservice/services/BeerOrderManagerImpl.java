package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.brewery.model.events.ValidateOrderResponse;
import guru.springframework.beerorderservice.domain.BeerOrder;
import guru.springframework.beerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.repositories.BeerOrderRepository;
import guru.springframework.beerorderservice.sm.StateMachineService;
import guru.springframework.beerorderservice.util.BeerOrderConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BeerOrderManagerImpl implements BeerOrderManager {

  private final StateMachineService stateMachineService;
  private final BeerOrderRepository beerOrderRepository;

  @Override
  public BeerOrder create(BeerOrder entity) {
    entity.setId(null);
    entity.setOrderStatus(BeerOrderStatusEnum.NEW);
    BeerOrder order = beerOrderRepository.save(entity);
    sendEvent(order, BeerOrderEventEnum.VALIDATE_ORDER);
    return order;
  }

  @Override
  public void handle(ValidateOrderResponse response) {
    final BeerOrder order = beerOrderRepository.getOne(response.getOrderId());
    sendEvent(
        order,
        response.isValid()
            ? BeerOrderEventEnum.VALIDATION_PASSED
            : BeerOrderEventEnum.VALIDATION_FAILED);
  }

  private void sendEvent(BeerOrder order, BeerOrderEventEnum event) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine =
        stateMachineService.acquireStateMachine(order);
    Message<BeerOrderEventEnum> message =
        MessageBuilder.withPayload(event)
            .setHeader(BeerOrderConstants.ORDER_ID_HEADER, order.getId().toString())
            .build();
    stateMachine.sendEvent(message);
  }
}
