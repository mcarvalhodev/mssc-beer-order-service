package guru.springframework.beerorderservice.application;

import guru.springframework.beerorderservice.domain.event.AllocateOrderResponse;
import guru.springframework.beerorderservice.domain.event.ValidateOrderResponse;
import guru.springframework.beerorderservice.domain.model.order.*;
import guru.springframework.beerorderservice.sm.StateMachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderManagerImpl implements BeerOrderManager {

  private final StateMachineService stateMachineService;
  private final BeerOrderRepository beerOrderRepository;

  @Transactional
  @Override
  public BeerOrder create(BeerOrder entity) {
    entity.setId(null);
    entity.setOrderStatus(BeerOrderStatusEnum.NEW);
    BeerOrder order = beerOrderRepository.saveAndFlush(entity);
    sendEvent(order, BeerOrderEventEnum.VALIDATE_ORDER);
    return order;
  }

  @Transactional
  @Override
  public void handle(ValidateOrderResponse response) {
    final Optional<BeerOrder> beerOrderOptional =
        beerOrderRepository.findById(response.getOrderId());

    beerOrderOptional.ifPresentOrElse(
        order -> {
          sendEvent(
              order,
              response.isValid()
                  ? BeerOrderEventEnum.VALIDATION_PASSED
                  : BeerOrderEventEnum.VALIDATION_FAILED);

          if (response.isValid()) {
            sendEvent(
                beerOrderRepository.findById(order.getId()).get(),
                BeerOrderEventEnum.ALLOCATE_ORDER);
          }
        },
        () -> log.error("Order not found with id: " + response.getOrderId()));
  }

  @Override
  public Consumer<AllocateOrderResponse> onAllocationResult() {
    return message -> {
      final Optional<BeerOrder> beerOrderOptional =
          beerOrderRepository.findById(message.getOrder().getId());

      beerOrderOptional.ifPresentOrElse(
          order -> {
            BeerOrderEventEnum event =
                message.isAllocationError()
                    ? BeerOrderEventEnum.ALLOCATION_FAILED
                    : message.isPendingInventory()
                        ? BeerOrderEventEnum.ALLOCATION_NO_INVENTORY
                        : BeerOrderEventEnum.ALLOCATION_SUCCESS;

            sendEvent(order, event);
          },
          () -> log.error("Order not found with id: " + message.getOrder().getId()));
    };
  }

  @Override
  public void beerOrderPickedUp(UUID id) {
    final Optional<BeerOrder> orderOptional = beerOrderRepository.findById(id);

    orderOptional.ifPresentOrElse(
        beerOrder -> {
          sendEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);
        },
        () -> log.error("Order not found with id: " + id));
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
