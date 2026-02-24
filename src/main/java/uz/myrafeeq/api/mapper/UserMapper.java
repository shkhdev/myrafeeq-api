package uz.myrafeeq.api.mapper;

import org.mapstruct.Mapper;
import uz.myrafeeq.api.dto.response.UserResponse;
import uz.myrafeeq.api.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserResponse toUserResponse(UserEntity entity);
}
