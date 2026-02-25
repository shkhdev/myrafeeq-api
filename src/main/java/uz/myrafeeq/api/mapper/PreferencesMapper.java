package uz.myrafeeq.api.mapper;

import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.TimeFormat;

@Mapper(componentModel = "spring")
public abstract class PreferencesMapper {

  @Autowired private ObjectMapper objectMapper;

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
  @Mapping(
      target = "prayerNotifications",
      source = "entity.prayerNotifications",
      qualifiedByName = "jsonToBooleanMap")
  @Mapping(
      target = "manualAdjustments",
      source = "entity.manualAdjustments",
      qualifiedByName = "jsonToIntegerMap")
  public abstract UserPreferencesResponse toPreferencesResponse(
      UserPreferencesEntity entity, CityResponse city);

  @Named("enumName")
  String enumName(Enum<?> value) {
    return value != null ? value.name() : null;
  }

  @Named("timeFormatValue")
  String timeFormatValue(TimeFormat value) {
    return value != null ? value.getValue() : null;
  }

  @Named("jsonToBooleanMap")
  Map<String, Boolean> jsonToBooleanMap(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (JacksonException e) {
      return Map.of();
    }
  }

  @Named("jsonToIntegerMap")
  public Map<String, Integer> jsonToIntegerMap(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (JacksonException e) {
      return Map.of();
    }
  }
}
