package uz.myrafeeq.api.service.city;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uz.myrafeeq.api.configuration.NominatimProperties;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.service.city.NominatimClient.NominatimPlace;

class NominatimClientTest {

  private NominatimClient nominatimClient;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    NominatimProperties properties =
        new NominatimProperties(
            "https://nominatim.openstreetmap.org",
            "TestAgent/1.0",
            50.0,
            CalculationMethod.MWL,
            Madhab.HANAFI);
    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    nominatimClient = new NominatimClient(properties, builder);
  }

  @Test
  void searchCities_shouldReturnResults_whenValidResponse() {
    String json =
        """
        [
          {
            "lat": "41.2994958",
            "lon": "69.2400734",
            "name": "Tashkent",
            "osm_id": 1991790,
            "osm_type": "relation",
            "addresstype": "city",
            "address": {
              "city": "Tashkent",
              "country": "Uzbekistan",
              "country_code": "uz"
            }
          }
        ]
        """;

    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    List<NominatimPlace> results = nominatimClient.searchCities("Tashkent", 5);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().name()).isEqualTo("Tashkent");
    assertThat(results.getFirst().addresstype()).isEqualTo("city");
    mockServer.verify();
  }

  @Test
  void searchCities_shouldFilterOutCountries() {
    String json =
        """
        [
          {
            "lat": "41.3237300",
            "lon": "63.9528098",
            "name": "Uzbekistan",
            "osm_id": 196240,
            "osm_type": "relation",
            "addresstype": "country",
            "address": {
              "country": "Uzbekistan",
              "country_code": "uz"
            }
          },
          {
            "lat": "41.2994958",
            "lon": "69.2400734",
            "name": "Tashkent",
            "osm_id": 1991790,
            "osm_type": "relation",
            "addresstype": "city",
            "address": {
              "city": "Tashkent",
              "country": "Uzbekistan",
              "country_code": "uz"
            }
          }
        ]
        """;

    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    List<NominatimPlace> results = nominatimClient.searchCities("Uzbekistan", 5);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().name()).isEqualTo("Tashkent");
    mockServer.verify();
  }

  @Test
  void searchCities_shouldFilterOutStates() {
    String json =
        """
        [
          {
            "lat": "41.2994958",
            "lon": "69.2400734",
            "name": "Tashkent",
            "osm_id": 1991790,
            "osm_type": "relation",
            "addresstype": "city",
            "address": {
              "city": "Tashkent",
              "country": "Uzbekistan",
              "country_code": "uz"
            }
          },
          {
            "lat": "41.3",
            "lon": "69.3",
            "name": "Tashkent Region",
            "osm_id": 12345,
            "osm_type": "relation",
            "addresstype": "state",
            "address": {
              "country": "Uzbekistan",
              "country_code": "uz"
            }
          }
        ]
        """;

    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    List<NominatimPlace> results = nominatimClient.searchCities("Tashkent", 5);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().addresstype()).isEqualTo("city");
    mockServer.verify();
  }

  @Test
  void searchCities_shouldReturnEmpty_whenServerError() {
    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withServerError());

    List<NominatimPlace> results = nominatimClient.searchCities("Test", 5);

    assertThat(results).isEmpty();
    mockServer.verify();
  }

  @Test
  void searchCities_shouldReturnEmpty_whenEmptyArray() {
    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    List<NominatimPlace> results = nominatimClient.searchCities("NoResults", 5);

    assertThat(results).isEmpty();
    mockServer.verify();
  }

  @Test
  void lookup_shouldReturnPlace_whenValidResponse() {
    String json =
        """
        [
          {
            "lat": "41.2994958",
            "lon": "69.2400734",
            "name": "Tashkent",
            "osm_id": 1991790,
            "osm_type": "relation",
            "addresstype": "city",
            "address": {
              "city": "Tashkent",
              "country": "Uzbekistan",
              "country_code": "uz"
            }
          }
        ]
        """;

    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/lookup")))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    Optional<NominatimPlace> result = nominatimClient.lookup("R1991790");

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo("Tashkent");
    mockServer.verify();
  }

  @Test
  void lookup_shouldReturnEmpty_whenServerError() {
    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/lookup")))
        .andRespond(withServerError());

    Optional<NominatimPlace> result = nominatimClient.lookup("R999");

    assertThat(result).isEmpty();
    mockServer.verify();
  }

  @Test
  void reverse_shouldReturnPlace_whenValidResponse() {
    String json =
        """
        {
          "lat": "41.2994958",
          "lon": "69.2400734",
          "name": "Tashkent",
          "osm_id": 1991790,
          "osm_type": "relation",
          "addresstype": "city",
          "address": {
            "city": "Tashkent",
            "country": "Uzbekistan",
            "country_code": "uz"
          }
        }
        """;

    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/reverse")))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    Optional<NominatimPlace> result = nominatimClient.reverse(41.3, 69.24);

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo("Tashkent");
    mockServer.verify();
  }

  @Test
  void reverse_shouldReturnEmpty_whenServerError() {
    mockServer
        .expect(requestTo(org.hamcrest.Matchers.containsString("/reverse")))
        .andRespond(withServerError());

    Optional<NominatimPlace> result = nominatimClient.reverse(41.3, 69.24);

    assertThat(result).isEmpty();
    mockServer.verify();
  }
}
