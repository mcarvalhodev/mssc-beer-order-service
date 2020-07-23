package guru.springframework.beerorderservice.domain.model.order;

public enum BeerOrderEventEnum {
  VALIDATE_ORDER,
  VALIDATION_PASSED,
  VALIDATION_FAILED,
  ALLOCATE_ORDER,
  ALLOCATION_SUCCESS,
  ALLOCATION_NO_INVENTORY,
  ALLOCATION_FAILED,
  CANCEL_ORDER,
  BEERORDER_PICKED_UP
}
