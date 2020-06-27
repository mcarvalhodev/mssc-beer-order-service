package guru.springframework.beerorderservice.domain.event;

import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidateOrderRequest {

  private BeerOrderDto order;
}
