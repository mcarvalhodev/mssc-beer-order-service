package guru.springframework.beerorderservice.sm.actions;

import guru.springframework.beerorderservice.brewery.model.events.ValidateOrderRequest;
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
@RequiredArgsConstructor
@Component("validateOrder")
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final BeerOrderRepository beerOrderRepository;
  private final JmsTemplate jmsTemplate;
  private BeerOrderMapper beerOrderMapper;

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

    UUID uuid = OrderContextSupport.getOrderIdHeader(stateContext);
    BeerOrder order = beerOrderRepository.findOneById(uuid);
    jmsTemplate.convertAndSend(
        JmsConfig.VALIDATE_ORDER_QUEUE,
        ValidateOrderRequest.builder().order(beerOrderMapper.beerOrderToDto(order)).build());
    log.debug("Sent request to queue for order[id=" + order.getId() + "]");
  }
}
