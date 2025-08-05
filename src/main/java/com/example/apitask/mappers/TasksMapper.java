package com.example.apitask.mappers;

import com.example.apitask.dtos.tasks.TasksRequestDTO;
import com.example.apitask.dtos.tasks.TasksResponseDTO;
import com.example.apitask.models.Tasks;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = CategoriesMapper.class)
public interface TasksMapper {
    Tasks toEntity(TasksRequestDTO tasksRequestDTO);
    @Mapping(target = "categories", source = "categories")
    TasksResponseDTO toDTO(Tasks tasks);
    List<TasksResponseDTO> toListResponse(List<Tasks> tasks);
}
