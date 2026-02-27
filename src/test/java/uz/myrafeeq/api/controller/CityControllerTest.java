package uz.myrafeeq.api.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.myrafeeq.api.configuration.AdminProperties;
import uz.myrafeeq.api.configuration.RateLimitProperties;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.CitySearchResponse;
import uz.myrafeeq.api.dto.response.NearestCityResponse;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.security.JwtTokenProvider;
import uz.myrafeeq.api.service.city.CityService;

@WebMvcTest(CityController.class)
class CityControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private CityService cityService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private RateLimitProperties rateLimitProperties;
  @MockitoBean private AdminProperties adminProperties;

  @Test
  @WithMockUser
  void should_returnCities_when_searchWithValidQuery() throws Exception {
    CityResponse city =
        CityResponse.builder()
            .id("tashkent")
            .name("Tashkent")
            .country("UZ")
            .latitude(41.2995)
            .longitude(69.2401)
            .timezone("Asia/Tashkent")
            .build();
    CitySearchResponse response = CitySearchResponse.builder().cities(List.of(city)).build();

    given(cityService.searchCities("Tashkent", 10)).willReturn(response);

    mockMvc
        .perform(get("/api/v1/cities").param("q", "Tashkent").param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cities").isArray())
        .andExpect(jsonPath("$.cities[0].name").value("Tashkent"))
        .andExpect(jsonPath("$.cities[0].country").value("UZ"));
  }

  @Test
  @WithMockUser
  void should_returnEmptyList_when_noMatches() throws Exception {
    CitySearchResponse response = CitySearchResponse.builder().cities(List.of()).build();

    given(cityService.searchCities("Unknown", 10)).willReturn(response);

    mockMvc
        .perform(get("/api/v1/cities").param("q", "Unknown"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cities").isEmpty());
  }

  @Test
  @WithMockUser
  void should_returnNearestCity_when_validCoordinates() throws Exception {
    CityResponse city =
        CityResponse.builder().id("tashkent").name("Tashkent").country("UZ").build();
    NearestCityResponse response =
        NearestCityResponse.builder().city(city).distanceKm(1.23).build();

    given(cityService.findNearestCity(41.2995, 69.2401)).willReturn(response);

    mockMvc
        .perform(get("/api/v1/cities/nearest").param("lat", "41.2995").param("lon", "69.2401"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.city.name").value("Tashkent"))
        .andExpect(jsonPath("$.distanceKm").value(1.23));
  }

  @Test
  @WithMockUser
  void should_return404_when_noCityFound() throws Exception {
    given(cityService.findNearestCity(0.0, 0.0))
        .willThrow(new CityNotFoundException("No cities found"));

    mockMvc
        .perform(get("/api/v1/cities/nearest").param("lat", "0.0").param("lon", "0.0"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("CITY_NOT_FOUND"));
  }

  @Test
  @WithMockUser
  void should_useDefaultLimit_when_limitNotProvided() throws Exception {
    CitySearchResponse response = CitySearchResponse.builder().cities(List.of()).build();

    given(cityService.searchCities("Test", 10)).willReturn(response);

    mockMvc.perform(get("/api/v1/cities").param("q", "Test")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void should_returnError_when_missingRequiredQueryParam() throws Exception {
    mockMvc.perform(get("/api/v1/cities")).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void should_returnError_when_missingLatLon() throws Exception {
    mockMvc
        .perform(get("/api/v1/cities/nearest").param("lat", "41.3"))
        .andExpect(status().isBadRequest());
  }
}
