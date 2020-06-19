package guru.springframework.beerorderservice.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateOrderResponse {

  private UUID orderId;
  private boolean valid;
}
