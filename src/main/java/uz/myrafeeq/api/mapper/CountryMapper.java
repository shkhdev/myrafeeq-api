package uz.myrafeeq.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.myrafeeq.api.dto.response.CountryResponse;
import uz.myrafeeq.api.entity.CountryEntity;

@Mapper(componentModel = "spring")
public interface CountryMapper {

  @Mapping(
      target = "defaultMethod",
      expression =
          "java(entity.getDefaultMethod() != null ? entity.getDefaultMethod().name() : null)")
  @Mapping(
      target = "defaultMadhab",
      expression =
          "java(entity.getDefaultMadhab() != null ? entity.getDefaultMadhab().name() : null)")
  CountryResponse toCountryResponse(CountryEntity entity);
}
