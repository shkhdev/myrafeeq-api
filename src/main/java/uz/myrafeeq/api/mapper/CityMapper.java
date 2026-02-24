package uz.myrafeeq.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.entity.CityEntity;

@Mapper(componentModel = "spring")
public interface CityMapper {

  @Mapping(target = "country", source = "country.code")
  @Mapping(target = "defaultMethod", source = "country.defaultMethod")
  @Mapping(target = "defaultMadhab", source = "country.defaultMadhab")
  CityResponse toCityResponse(CityEntity entity);
}
