package guru.springframework.beerorderservice.sm;

import guru.springframework.beerorderservice.domain.model.order.BeerOrder;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderRepository;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.sm.interceptors.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StateMachineService {

  private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;
  private final BeerOrderStateChangeInterceptor interceptor;
  private final BeerOrderRepository beerOrderRepository;

  public StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> acquireStateMachine(UUID id) {
    BeerOrder order = beerOrderRepository.findById(id).orElseThrow();
    return build(order);
  }

  private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder order) {
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

  public StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> acquireStateMachine(
      BeerOrder order) {
    return build(order);
  }
}
