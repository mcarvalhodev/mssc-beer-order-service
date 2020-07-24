package guru.springframework.beerorderservice.interfaces.rest.controller;

import guru.springframework.beerorderservice.services.CustomerPagedList;
import guru.springframework.beerorderservice.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/")
@RestController
public class CustomerController {

  private final CustomerService customerService;

  @GetMapping
  public CustomerPagedList listCustomers(
      @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {

    return customerService.listCustomers(PageRequest.of(0, 25));
  }
}
