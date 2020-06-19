package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.brewery.model.AllocateOrderResponse;
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

import java.util.function.Consumer;

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
    if (response.isValid()) {
      sendEvent(beerOrderRepository.findOneById(order.getId()), BeerOrderEventEnum.ALLOCATE_ORDER);
    }
  }

  @Override
  public Consumer<AllocateOrderResponse> onAllocationResult() {
    return message -> {
      final BeerOrder order = beerOrderRepository.getOne(message.getOrder().getId());
      BeerOrderEventEnum event =
          message.isAllocationError()
              ? BeerOrderEventEnum.ALLOCATION_FAILED
              : message.isPendingInventory()
                  ? BeerOrderEventEnum.ALLOCATION_NO_INVENTORY
                  : BeerOrderEventEnum.ALLOCATION_SUCCESS;

      sendEvent(order, event);
    };
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
