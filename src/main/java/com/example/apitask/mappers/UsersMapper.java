package com.example.apitask.mappers;

import com.example.apitask.dtos.users.UsersRequestDTO;
import com.example.apitask.dtos.users.UsersResponseDTO;
import com.example.apitask.models.Users;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    Users toEntity(UsersRequestDTO usersRequestDTO);
    UsersResponseDTO toDTO(Users users);
    List<UsersResponseDTO> toListResponse(List<Users> users);
}
