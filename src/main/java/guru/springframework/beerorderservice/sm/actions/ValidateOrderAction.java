package guru.springframework.beerorderservice.sm.actions;

import guru.springframework.beerorderservice.domain.event.ValidateOrderRequest;
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

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component("validateOrder")
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final BeerOrderRepository beerOrderRepository;
  private final JmsTemplate jmsTemplate;
  private final BeerOrderMapper beerOrderMapper;

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

    UUID uuid = OrderContextSupport.getOrderIdHeader(stateContext);
    BeerOrder order = beerOrderRepository.findById(uuid).get();
    jmsTemplate.convertAndSend(
        JmsConfig.VALIDATE_ORDER_QUEUE,
        ValidateOrderRequest.builder().order(beerOrderMapper.beerOrderToDto(order)).build());
    log.debug("Sent request to queue for order[id=" + order.getId() + "]");
  }
}
