package guru.springframework.beerorderservice.brewery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocateOrderResponse {

  private boolean allocationError;
  private boolean pendingInventory;
  private BeerOrderDto order;
}
