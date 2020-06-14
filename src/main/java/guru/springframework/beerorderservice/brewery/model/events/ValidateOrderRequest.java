package guru.springframework.beerorderservice.brewery.model.events;

import guru.springframework.beerorderservice.brewery.model.BeerOrderDto;
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
