package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.web.model.BeerDto;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
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
