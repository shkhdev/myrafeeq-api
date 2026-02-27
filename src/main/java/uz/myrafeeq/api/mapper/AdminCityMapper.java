package uz.myrafeeq.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.myrafeeq.api.dto.response.AdminCityResponse;
import uz.myrafeeq.api.entity.CityEntity;

@Mapper(componentModel = "spring")
public interface AdminCityMapper {

  @Mapping(target = "countryCode", source = "country.code")
  @Mapping(target = "countryName", source = "country.name")
  AdminCityResponse toAdminCityResponse(CityEntity entity);
}
