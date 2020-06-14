package guru.springframework.beerorderservice.sm.interceptors;

import guru.springframework.beerorderservice.domain.BeerOrder;
import guru.springframework.beerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.repositories.BeerOrderRepository;
import guru.springframework.beerorderservice.util.BeerOrderConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Component
public class BeerOrderStateChangeInterceptor
    extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final BeerOrderRepository beerOrderRepository;

  @Override
  public void preStateChange(
      State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
      Message<BeerOrderEventEnum> message,
      Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
      StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
    ofNullable(message)
        .flatMap(
            orderEventMessage ->
                ofNullable(
                    orderEventMessage
                        .getHeaders()
                        .get(BeerOrderConstants.ORDER_ID_HEADER, String.class)))
        .ifPresent(
            orderId -> {
              BeerOrder beerOrder =
                  beerOrderRepository.findById(UUID.fromString(orderId)).orElseThrow();
              beerOrder.setOrderStatus(state.getId());
              beerOrderRepository.saveAndFlush(beerOrder);
            });
  }
}
