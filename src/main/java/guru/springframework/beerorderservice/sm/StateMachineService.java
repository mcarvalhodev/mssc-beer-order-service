package guru.springframework.beerorderservice.sm;

import guru.springframework.beerorderservice.domain.BeerOrder;
import guru.springframework.beerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.sm.interceptors.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StateMachineService {

  private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;
  private final BeerOrderStateChangeInterceptor interceptor;

  public StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> acquireStateMachine(
      BeerOrder order) {
    final StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> machine =
        this.factory.getStateMachine(order.getId());
    machine.stop();
    machine
        .getStateMachineAccessor()
        .doWithAllRegions(
            sma -> {
              sma.addStateMachineInterceptor(interceptor);
              sma.resetStateMachine(
                  new DefaultStateMachineContext<>(order.getOrderStatus(), null, null, null));
            });
    machine.start();
    return machine;
  }
}
