package guru.springframework.beerorderservice.domain.model.beer;

import guru.springframework.beerorderservice.interfaces.rest.model.BeerDto;

import java.util.Optional;

public interface BeerService {
    Optional<BeerDto> getBeerByUpc(String upc);
}
