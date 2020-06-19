package guru.springframework.beerorderservice.sm.actions;

import guru.springframework.beerorderservice.config.JmsConfig;
import guru.springframework.beerorderservice.domain.BeerOrder;
import guru.springframework.beerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.repositories.BeerOrderRepository;
import guru.springframework.beerorderservice.sm.support.OrderContextSupport;
import guru.springframework.beerorderservice.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("allocateOrder")
@RequiredArgsConstructor
public class AllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final JmsTemplate jmsTemplate;
  private final BeerOrderRepository orderRepository;
  private final BeerOrderMapper orderMapper;

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
    UUID uuid = OrderContextSupport.getOrderIdHeader(stateContext);
    BeerOrder order = orderRepository.findOneById(uuid);

    jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, orderMapper.beerOrderToDto(order));

    log.debug("Sent allocation request for Order[id=" + order.getId() + "]");
  }
}
