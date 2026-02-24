package uz.myrafeeq.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.enums.SupportedLocale;

@Mapper(componentModel = "spring")
public interface CityMapper {

  @Mapping(target = "name", source = "nameEn")
  @Mapping(target = "country", source = "countryCode")
  CityResponse toCityResponse(CityEntity entity);

  default String resolveLocalizedName(CityEntity entity, SupportedLocale locale) {
    return switch (locale) {
      case AR -> entity.getNameAr();
      case UZ -> entity.getNameUz();
      case RU -> entity.getNameRu();
      case EN -> entity.getNameEn();
    };
  }

  default CityResponse toCityResponse(CityEntity entity, SupportedLocale locale) {
    return CityResponse.builder()
        .id(entity.getId())
        .name(resolveLocalizedName(entity, locale))
        .country(entity.getCountryCode())
        .latitude(entity.getLatitude())
        .longitude(entity.getLongitude())
        .build();
  }
}
