package com.datagram.datagramweb.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MunicipioRestService {
  private final RestClient client;
  private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
  private static final Set<String> STATES =
      Set.of(
          "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB",
          "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

  public MunicipioRestService(RestClient.Builder builder) {
    var factory =
        new JdkClientHttpRequestFactory(
            java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
    factory.setReadTimeout(Duration.ofSeconds(5));
    client =
        builder
            .baseUrl("https://servicodados.ibge.gov.br/api/v1/localidades")
            .requestFactory(factory)
            .build();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record City(String nome) {}

  public List<String> listarMunicipios(String uf) {
    if (uf == null || !STATES.contains(uf))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF inválida");
    return cache.computeIfAbsent(
        uf,
        key -> {
          try {
            City[] result =
                client.get().uri("/estados/{uf}/municipios", key).retrieve().body(City[].class);
            if (result == null) throw new IllegalStateException();
            return Arrays.stream(result).map(City::nome).sorted().toList();
          } catch (RuntimeException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY, "Consulta de municípios indisponível");
          }
        });
  }
}
