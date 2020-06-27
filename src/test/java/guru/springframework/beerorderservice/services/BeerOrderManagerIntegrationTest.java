package guru.springframework.beerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.beerorderservice.application.BeerOrderManager;
import guru.springframework.beerorderservice.domain.model.beer.BeerServiceImpl;
import guru.springframework.beerorderservice.domain.model.customer.Customer;
import guru.springframework.beerorderservice.domain.model.customer.CustomerRepository;
import guru.springframework.beerorderservice.domain.model.order.BeerOrder;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderLine;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderRepository;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerDto;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerIntegrationTest {

  @Autowired BeerOrderManager beerOrderManager;
  @Autowired BeerOrderRepository orderRepository;

  @Autowired CustomerRepository customerRepository;

  @Autowired ObjectMapper objectMapper;

  @Autowired WireMockServer wireMockServer;

  Customer customer;

  UUID beerId = UUID.randomUUID();

  @TestConfiguration
  static class RestTemplateBuilderProvider {

    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer() {
      final ManagedWireMockServer server = with(wireMockConfig().port(8083));
      server.start();
      return server;
    }
  }

  @BeforeEach
  void setUp() {
    this.customer =
        customerRepository.save(Customer.builder().customerName("Test customer").build());
  }

  @Test
  void testNewToAllocate() throws JsonProcessingException, InterruptedException {

    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();
    final BeerPagedList pagedList = new BeerPagedList(Collections.singletonList(beerDto));

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    final BeerOrder order = beerOrderManager.create(newOne());
    assertNotNull(order);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.ALLOCATED, beerOrder.getOrderStatus());
            });
  }

  @Test
  public void testNewToPickedUp() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();
    final BeerPagedList pagedList = new BeerPagedList(Collections.singletonList(beerDto));

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    final BeerOrder order = beerOrderManager.create(newOne());
    assertNotNull(order);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.ALLOCATED, beerOrder.getOrderStatus());
            });

    beerOrderManager.beerOrderPickedUp(order.getId());

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.PICKED_UP, beerOrder.getOrderStatus());
            });
    assertEquals(
        BeerOrderStatusEnum.PICKED_UP,
        orderRepository.findById(order.getId()).get().getOrderStatus());
  }

  public BeerOrder newOne() {
    final BeerOrder order = BeerOrder.builder().customer(customer).build();
    final BeerOrderLine orderLine =
        BeerOrderLine.builder()
            .beerId(beerId)
            .upc("1234")
            .orderQuantity(1)
            .beerOrder(order)
            .build();
    final Set<BeerOrderLine> orderLines = new HashSet<>();
    orderLines.add(orderLine);
    order.setBeerOrderLines(orderLines);
    return order;
  }
}
