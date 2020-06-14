package guru.springframework.beerorderservice.sm.support;

import guru.springframework.beerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.beerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.util.BeerOrderConstants;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;

import java.util.UUID;

public final class OrderContextSupport {

  private OrderContextSupport() {}

  public static UUID getOrderIdHeader(
      StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
    String header =
        stateContext
            .getMessage()
            .getHeaders()
            .get(BeerOrderConstants.ORDER_ID_HEADER, String.class);
    return UUID.fromString(header);
  }

  public static UUID getOrderIdHeader(Message message) {
    String header = message.getHeaders().get(BeerOrderConstants.ORDER_ID_HEADER, String.class);
    return UUID.fromString(header);
  }
}
