package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.domain.model.customer.Customer;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CustomerPagedList extends PageImpl<Customer> {
  public CustomerPagedList(List<Customer> content, Pageable pageable, long total) {
    super(content, pageable, total);
  }
}
