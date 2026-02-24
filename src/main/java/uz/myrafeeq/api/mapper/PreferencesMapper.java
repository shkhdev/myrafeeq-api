package uz.myrafeeq.api.mapper;

import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.entity.UserPreferencesEntity;

@Mapper(componentModel = "spring")
public interface PreferencesMapper {

  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(target = "city", source = "city")
  @Mapping(
      target = "calculationMethod",
      expression =
          "java(entity.getCalculationMethod() != null ? entity.getCalculationMethod().name() : null)")
  @Mapping(
      target = "madhab",
      expression = "java(entity.getMadhab() != null ? entity.getMadhab().name() : null)")
  @Mapping(
      target = "highLatitudeRule",
      expression =
          "java(entity.getHighLatitudeRule() != null ? entity.getHighLatitudeRule().name() : null)")
  @Mapping(
      target = "timeFormat",
      expression =
          "java(entity.getTimeFormat() != null ? entity.getTimeFormat().getValue() : null)")
  @Mapping(
      target = "theme",
      expression = "java(entity.getTheme() != null ? entity.getTheme().name() : null)")
  @Mapping(
      target = "reminderTiming",
      expression =
          "java(entity.getReminderTiming() != null ? entity.getReminderTiming().name() : null)")
  @Mapping(
      target = "prayerNotifications",
      source = "entity.prayerNotifications",
      qualifiedByName = "jsonToBooleanMap")
  @Mapping(
      target = "manualAdjustments",
      source = "entity.manualAdjustments",
      qualifiedByName = "jsonToIntegerMap")
  UserPreferencesResponse toPreferencesResponse(UserPreferencesEntity entity, CityResponse city);

  @Named("jsonToBooleanMap")
  default Map<String, Boolean> jsonToBooleanMap(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
    } catch (JacksonException e) {
      return Map.of();
    }
  }

  @Named("jsonToIntegerMap")
  default Map<String, Integer> jsonToIntegerMap(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
    } catch (JacksonException e) {
      return Map.of();
    }
  }
}
