package guru.springframework.beerorderservice.services;

import guru.springframework.beerorderservice.domain.model.customer.Customer;
import guru.springframework.beerorderservice.domain.model.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;

  @Override
  public CustomerPagedList listCustomers(Pageable page) {
    final Page<Customer> pagination = customerRepository.findAll(page);

    final List<Customer> customerList = pagination.stream().collect(Collectors.toList());
    final PageRequest pageRequest =
        PageRequest.of(
            pagination.getPageable().getPageNumber(), pagination.getPageable().getPageSize());

    return new CustomerPagedList(customerList, pageRequest, pagination.getTotalElements());
  }
}
