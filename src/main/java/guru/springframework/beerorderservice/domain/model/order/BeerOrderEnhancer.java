package guru.springframework.beerorderservice.domain.model.order;

import guru.springframework.beerorderservice.domain.model.beer.BeerService;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerDto;
import guru.springframework.beerorderservice.interfaces.rest.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class BeerOrderEnhancer {

    private final BeerService beerService;

    public BeerOrderDto enhance(BeerOrderDto dto) {

        dto.getBeerOrderLines()
                .forEach(
                        beerOrderLineDto -> {
                            Optional<BeerDto> response = beerService.getBeerByUpc(beerOrderLineDto.getUpc());
                            response.ifPresent(
                                    beerDto -> {
                                        beerOrderLineDto.setBeerId(beerDto.getId());
                                        beerOrderLineDto.setBeerName(beerDto.getBeerName());
                                        beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
                                    });
                        });

        return dto;
    }
}
