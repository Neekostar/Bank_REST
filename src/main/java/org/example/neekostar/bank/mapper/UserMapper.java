package org.example.neekostar.bank.mapper;

import org.example.neekostar.bank.dto.UserResponseDto;
import org.example.neekostar.bank.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);
}
