package guru.springframework.beerorderservice.services.beer;

import guru.springframework.beerorderservice.brewery.model.BeerDto;

import java.util.Optional;

public interface BeerService {
    Optional<BeerDto> getBeerByUpc(String upc);
}
