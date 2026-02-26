package uz.myrafeeq.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.TimeFormat;

@Mapper(componentModel = "spring")
public interface PreferencesMapper {

  @Mapping(target = "city", source = "city")
  @Mapping(
      target = "calculationMethod",
      source = "entity.calculationMethod",
      qualifiedByName = "enumName")
  @Mapping(target = "madhab", source = "entity.madhab", qualifiedByName = "enumName")
  @Mapping(
      target = "highLatitudeRule",
      source = "entity.highLatitudeRule",
      qualifiedByName = "enumName")
  @Mapping(target = "timeFormat", source = "entity.timeFormat", qualifiedByName = "timeFormatValue")
  @Mapping(target = "theme", source = "entity.theme", qualifiedByName = "enumName")
  @Mapping(
      target = "reminderTiming",
      source = "entity.reminderTiming",
      qualifiedByName = "enumName")
  UserPreferencesResponse toPreferencesResponse(UserPreferencesEntity entity, CityResponse city);

  @Named("enumName")
  default String enumName(Enum<?> value) {
    return value != null ? value.name() : null;
  }

  @Named("timeFormatValue")
  default String timeFormatValue(TimeFormat value) {
    return value != null ? value.getValue() : null;
  }
}
