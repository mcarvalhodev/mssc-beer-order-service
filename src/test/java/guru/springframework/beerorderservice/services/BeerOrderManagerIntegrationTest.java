package guru.springframework.beerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.beerorderservice.application.BeerOrderManager;
import guru.springframework.beerorderservice.domain.event.AllocationFailedEvent;
import guru.springframework.beerorderservice.domain.event.DeallocateOrderRequest;
import guru.springframework.beerorderservice.domain.model.beer.BeerServiceImpl;
import guru.springframework.beerorderservice.domain.model.customer.Customer;
import guru.springframework.beerorderservice.domain.model.customer.CustomerRepository;
import guru.springframework.beerorderservice.domain.model.order.BeerOrder;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderLine;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderRepository;
import guru.springframework.beerorderservice.domain.model.order.BeerOrderStatusEnum;
import guru.springframework.beerorderservice.infrastructure.jms.JmsConfig;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
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

  @Autowired JmsTemplate jms;

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

  @Test
  void testFailedValidation() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder newly = newOne();
    newly.setCustomerRef("fail-validation");

    final BeerOrder order = beerOrderManager.create(newly);

    assertNotNull(order);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, beerOrder.getOrderStatus());
            });
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

  @Test
  void testAllocationFailed() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder newly = newOne();
    newly.setCustomerRef("fail-allocation");

    final BeerOrder order = beerOrderManager.create(newly);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, beerOrder.getOrderStatus());
            });

    final AllocationFailedEvent event =
        (AllocationFailedEvent) jms.receiveAndConvert(JmsConfig.ALLOCATE_FAILURE_QUEUE);

    assertNotNull(event);
    assertThat(event.getOrderId()).isEqualTo(order.getId());
  }

  @Test
  void testPartialAllocation() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder newly = newOne();
    newly.setCustomerRef("partial-allocation");

    final BeerOrder order = beerOrderManager.create(newly);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, beerOrder.getOrderStatus());
            });
  }

  @Test
  void testValidationPendingToCancel() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder newly = newOne();
    newly.setCustomerRef("dont-validate");
    final BeerOrder order = beerOrderManager.create(newly);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, beerOrder.getOrderStatus());
            });

    beerOrderManager.cancelOrder(order.getId());

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.CANCELLED, beerOrder.getOrderStatus());
            });
  }

  @Test
  void testAllocationPendingToCancel() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    BeerOrder newly = newOne();
    newly.setCustomerRef("dont-allocate");
    final BeerOrder order = beerOrderManager.create(newly);

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, beerOrder.getOrderStatus());
            });

    beerOrderManager.cancelOrder(order.getId());

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.CANCELLED, beerOrder.getOrderStatus());
            });
  }

  @Test
  void testAllocatedToCancel() throws JsonProcessingException {
    final BeerDto beerDto = BeerDto.builder().id(beerId).upc(1234L).build();

    wireMockServer.stubFor(
        get("http://localhost:8080" + BeerServiceImpl.BEER_UPC_PATH_V1 + "1234")
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

    final BeerOrder order = beerOrderManager.create(newOne());

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.ALLOCATED, beerOrder.getOrderStatus());
            });

    beerOrderManager.cancelOrder(order.getId());

    await()
        .untilAsserted(
            () -> {
              final BeerOrder beerOrder = orderRepository.findById(order.getId()).get();
              assertEquals(BeerOrderStatusEnum.CANCELLED, beerOrder.getOrderStatus());
            });

    final DeallocateOrderRequest event =
        (DeallocateOrderRequest) jms.receiveAndConvert(JmsConfig.DEALLOCATE_ORDER_QUEUE);

    assertNotNull(event);
    assertThat(event.getOrder().getId()).isEqualTo(order.getId());
  }
}
