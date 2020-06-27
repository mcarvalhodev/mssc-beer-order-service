package guru.springframework.beerorderservice.sm.actions;

import guru.springframework.beerorderservice.domain.model.order.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.sm.support.OrderContextSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("validationFailure")
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {

    final UUID orderId = OrderContextSupport.getOrderIdHeader(context);
    log.error("Compesating transaction...Validation failed:" + orderId);
  }
}
