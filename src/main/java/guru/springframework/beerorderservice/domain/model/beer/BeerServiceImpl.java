package guru.springframework.beerorderservice.domain.model.beer;

import guru.springframework.beerorderservice.interfaces.rest.model.BeerDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.StringJoiner;

@Service
public class BeerServiceImpl implements BeerService {

  private final RestTemplate restTemplate;

  public static final String BASE_PATH =
      new StringJoiner("/", "/", "/").add("api").add("v1").toString();

  public static final String BEER_PATH_V1 = BASE_PATH + "beer/";
  public static final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";

  public BeerServiceImpl(RestTemplateBuilder builder) {
    this.restTemplate = builder.build();
  }

  @Override
  public Optional<BeerDto> getBeerByUpc(String upc) {
    try {
      ResponseEntity<BeerDto> response =
          restTemplate.exchange(
              "http://localhost:8080" + BEER_UPC_PATH_V1 + upc,
              HttpMethod.GET,
              null,
              BeerDto.class);
      return Optional.of(response.getBody());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
