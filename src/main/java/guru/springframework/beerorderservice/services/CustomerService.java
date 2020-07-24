package guru.springframework.beerorderservice.services;

import org.springframework.data.domain.Pageable;

public interface CustomerService {
  CustomerPagedList listCustomers(Pageable page);
}
