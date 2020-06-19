package guru.springframework.beerorderservice.services.listeners;

public interface Listener<T> {

  void listen(T payload);
}
