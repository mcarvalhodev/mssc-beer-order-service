package guru.springframework.beerorderservice.sm.actions;

import guru.springframework.beerorderservice.domain.event.DeallocateOrderRequest;
import guru.springframework.beerorderservice.domain.model.order.BeerOrder;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderRepository;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.infrastructure.jms.JmsConfig;
import guru.springframework.beerorderservice.sm.support.OrderContextSupport;
import guru.springframework.beerorderservice.web.order.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("deallocateOrder")
@RequiredArgsConstructor
@Slf4j
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final JmsTemplate jmsTemplate;
  private final BeerOrderRepository orderRepository;
  private final BeerOrderMapper orderMapper;

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
    UUID uuid = OrderContextSupport.getOrderIdHeader(stateContext);
    Optional<BeerOrder> orderOptional = orderRepository.findById(uuid);

    orderOptional.ifPresentOrElse(
        order -> {
          jmsTemplate.convertAndSend(
              JmsConfig.DEALLOCATE_ORDER_QUEUE,
              DeallocateOrderRequest.builder().order(orderMapper.beerOrderToDto(order)).build());

          log.debug("Sent deallocation request for Order[id=" + order.getId() + "]");
        },
        () -> log.error("OrderNotFound{" + uuid + "}"));
  }
}
