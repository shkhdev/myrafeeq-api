package uz.myrafeeq.api.service.admin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.request.BulkCreateCitiesRequest;
import uz.myrafeeq.api.dto.request.CreateCityRequest;
import uz.myrafeeq.api.dto.request.UpdateCityRequest;
import uz.myrafeeq.api.dto.response.AdminCityResponse;
import uz.myrafeeq.api.dto.response.BulkCreateCitiesResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.CountryNotFoundException;
import uz.myrafeeq.api.exception.RequestValidationException;
import uz.myrafeeq.api.mapper.AdminCityMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCityService {

  private final CityRepository cityRepository;
  private final CountryRepository countryRepository;
  private final UserPreferencesRepository userPreferencesRepository;
  private final AdminCityMapper adminCityMapper;

  @Transactional(readOnly = true)
  public Page<AdminCityResponse> listCities(String countryCode, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name"));

    Page<CityEntity> cities =
        countryCode != null
            ? cityRepository.findByCountryCode(countryCode, pageRequest)
            : cityRepository.findAllWithCountry(pageRequest);

    return cities.map(adminCityMapper::toAdminCityResponse);
  }

  @Transactional(readOnly = true)
  public AdminCityResponse getCity(String id) {
    return adminCityMapper.toAdminCityResponse(findOrThrow(id));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public AdminCityResponse createCity(CreateCityRequest request) {
    if (cityRepository.existsById(request.getId())) {
      throw new RequestValidationException("City with id '" + request.getId() + "' already exists");
    }

    CountryEntity country = findCountryOrThrow(request.getCountryCode());

    CityEntity entity =
        CityEntity.builder()
            .id(request.getId())
            .name(request.getName())
            .country(country)
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .timezone(request.getTimezone())
            .build();

    cityRepository.save(entity);
    log.info("Created city: {}", entity.getId());
    return adminCityMapper.toAdminCityResponse(entity);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public BulkCreateCitiesResponse bulkCreateCities(BulkCreateCitiesRequest request) {
    Set<String> countryCodes =
        request.getCities().stream()
            .map(CreateCityRequest::getCountryCode)
            .collect(Collectors.toSet());

    List<CountryEntity> countries = countryRepository.findAllById(countryCodes);
    if (countries.size() != countryCodes.size()) {
      Set<String> found =
          countries.stream().map(CountryEntity::getCode).collect(Collectors.toSet());
      countryCodes.removeAll(found);
      throw new RequestValidationException("Countries not found: " + countryCodes);
    }

    var countryMap = countries.stream().collect(Collectors.toMap(CountryEntity::getCode, c -> c));

    List<CityEntity> entities =
        request.getCities().stream()
            .map(
                req ->
                    CityEntity.builder()
                        .id(req.getId())
                        .name(req.getName())
                        .country(countryMap.get(req.getCountryCode()))
                        .latitude(req.getLatitude())
                        .longitude(req.getLongitude())
                        .timezone(req.getTimezone())
                        .build())
            .toList();

    List<CityEntity> saved = cityRepository.saveAll(entities);
    log.info("Bulk created {} cities", saved.size());

    List<AdminCityResponse> responses =
        saved.stream().map(adminCityMapper::toAdminCityResponse).toList();

    return BulkCreateCitiesResponse.builder().created(saved.size()).cities(responses).build();
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public AdminCityResponse updateCity(String id, UpdateCityRequest request) {
    CityEntity entity = findOrThrow(id);
    entity.setName(request.getName());
    entity.setLatitude(request.getLatitude());
    entity.setLongitude(request.getLongitude());
    entity.setTimezone(request.getTimezone());

    cityRepository.save(entity);
    log.info("Updated city: {}", id);
    return adminCityMapper.toAdminCityResponse(entity);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public void deleteCity(String id) {
    CityEntity entity = findOrThrow(id);

    if (userPreferencesRepository.existsByCityId(id)) {
      throw new RequestValidationException(
          "Cannot delete city '" + id + "': user preferences reference it");
    }

    cityRepository.delete(entity);
    log.info("Deleted city: {}", id);
  }

  private CityEntity findOrThrow(String id) {
    return cityRepository
        .findById(id)
        .orElseThrow(() -> new CityNotFoundException("City not found: " + id));
  }

  private CountryEntity findCountryOrThrow(String code) {
    return countryRepository
        .findById(code)
        .orElseThrow(() -> new CountryNotFoundException("Country not found: " + code));
  }
}
