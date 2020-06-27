/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package guru.springframework.beerorderservice.domain.model.order;

import guru.springframework.beerorderservice.application.BeerOrderManager;
import guru.springframework.beerorderservice.domain.model.customer.Customer;
import guru.springframework.beerorderservice.domain.model.customer.CustomerRepository;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderDto;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderPagedList;
import guru.springframework.beerorderservice.services.BeerOrderDecorator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderServiceImpl implements BeerOrderService {

  private final BeerOrderRepository beerOrderRepository;
  private final CustomerRepository customerRepository;
  private final BeerOrderDecorator decorator;
  private final BeerOrderManager orderManager;

  @Override
  public BeerOrderPagedList listOrders(UUID customerId, Pageable pageable) {
    Optional<Customer> customerOptional = customerRepository.findById(customerId);

    if (customerOptional.isPresent()) {
      Page<BeerOrder> beerOrderPage =
          beerOrderRepository.findAllByCustomer(customerOptional.get(), pageable);

      return new BeerOrderPagedList(
          beerOrderPage.stream().map(decorator::beerOrderToDto).collect(Collectors.toList()),
          PageRequest.of(
              beerOrderPage.getPageable().getPageNumber(),
              beerOrderPage.getPageable().getPageSize()),
          beerOrderPage.getTotalElements());
    } else {
      return null;
    }
  }

  @Transactional
  @Override
  public BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto) {
    Optional<Customer> customerOptional = customerRepository.findById(customerId);

    if (customerOptional.isPresent()) {
      BeerOrder beerOrder = decorator.dtoToBeerOrder(beerOrderDto);
      beerOrder.setId(null); // should not be set by outside client
      beerOrder.setCustomer(customerOptional.get());
      beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

      beerOrder.getBeerOrderLines().forEach(line -> line.setBeerOrder(beerOrder));

      BeerOrder savedBeerOrder = orderManager.create(beerOrder);

      log.debug("Saved Beer Order: " + beerOrder.getId());

      return decorator.beerOrderToDto(savedBeerOrder);
    }
    // todo add exception type
    throw new RuntimeException("Customer Not Found");
  }

  @Override
  public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
    return decorator.beerOrderToDto(getOrder(customerId, orderId));
  }

  @Override
  public void pickupOrder(UUID customerId, UUID orderId) {
    orderManager.beerOrderPickedUp(orderId);
  }

  private BeerOrder getOrder(UUID customerId, UUID orderId) {
    Optional<Customer> customerOptional = customerRepository.findById(customerId);

    if (customerOptional.isPresent()) {
      Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(orderId);

      if (beerOrderOptional.isPresent()) {
        BeerOrder beerOrder = beerOrderOptional.get();

        // fall to exception if customer id's do not match - order not for customer
        if (beerOrder.getCustomer().getId().equals(customerId)) {
          return beerOrder;
        }
      }
      throw new RuntimeException("Beer Order Not Found");
    }
    throw new RuntimeException("Customer Not Found");
  }
}
