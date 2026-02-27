package uz.myrafeeq.api.service.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.request.CreateCountryRequest;
import uz.myrafeeq.api.dto.request.UpdateCountryRequest;
import uz.myrafeeq.api.dto.response.CountryResponse;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.exception.CountryNotFoundException;
import uz.myrafeeq.api.exception.RequestValidationException;
import uz.myrafeeq.api.mapper.CountryMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCountryService {

  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;
  private final CountryMapper countryMapper;

  @Transactional(readOnly = true)
  public List<CountryResponse> listCountries() {
    return countryRepository.findAll().stream().map(countryMapper::toCountryResponse).toList();
  }

  @Transactional(readOnly = true)
  public CountryResponse getCountry(String code) {
    return countryMapper.toCountryResponse(findOrThrow(code));
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public CountryResponse createCountry(CreateCountryRequest request) {
    if (countryRepository.existsById(request.getCode())) {
      throw new RequestValidationException(
          "Country with code '" + request.getCode() + "' already exists");
    }

    CountryEntity entity =
        CountryEntity.builder()
            .code(request.getCode())
            .name(request.getName())
            .defaultMethod(request.getDefaultMethod())
            .defaultMadhab(request.getDefaultMadhab())
            .build();

    countryRepository.save(entity);
    log.info("Created country: {}", entity.getCode());
    return countryMapper.toCountryResponse(entity);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public CountryResponse updateCountry(String code, UpdateCountryRequest request) {
    CountryEntity entity = findOrThrow(code);
    entity.setName(request.getName());
    entity.setDefaultMethod(request.getDefaultMethod());
    entity.setDefaultMadhab(request.getDefaultMadhab());

    countryRepository.save(entity);
    log.info("Updated country: {}", code);
    return countryMapper.toCountryResponse(entity);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "citySearch", allEntries = true),
        @CacheEvict(value = "nearestCity", allEntries = true)
      })
  public void deleteCountry(String code) {
    CountryEntity entity = findOrThrow(code);

    if (cityRepository.existsByCountryCode(code)) {
      throw new RequestValidationException(
          "Cannot delete country '" + code + "': cities reference it");
    }

    countryRepository.delete(entity);
    log.info("Deleted country: {}", code);
  }

  private CountryEntity findOrThrow(String code) {
    return countryRepository
        .findById(code)
        .orElseThrow(() -> new CountryNotFoundException("Country not found: " + code));
  }
}
