package guru.springframework.beerorderservice.services.beer;

import guru.springframework.beerorderservice.brewery.model.BeerDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class BeerServiceImpl implements BeerService {

    private final RestTemplate restTemplate;

    public BeerServiceImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        try {
            ResponseEntity<BeerDto> response =
                    restTemplate.exchange(
                            "http://localhost:8080/api/v1/beer/search?upc=" + upc,
                            HttpMethod.GET,
                            null,
                            BeerDto.class);
            return Optional.of(response.getBody());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
