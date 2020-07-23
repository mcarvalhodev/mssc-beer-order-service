package guru.springframework.beerorderservice.sm.actions;

import guru.springframework.beerorderservice.domain.event.AllocationFailedEvent;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.infrastructure.jms.JmsConfig;
import guru.springframework.beerorderservice.sm.support.OrderContextSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("allocationFailure")
@RequiredArgsConstructor
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final JmsTemplate jmsTemplate;

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

    final UUID uuid = OrderContextSupport.getOrderIdHeader(context);

    jmsTemplate.convertAndSend(
        JmsConfig.ALLOCATE_FAILURE_QUEUE, AllocationFailedEvent.builder().orderId(uuid).build());

    log.debug("Sent allocation failure message to queue for order id " + uuid);
  }
}
