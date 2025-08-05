package com.example.apitask.mappers;

import com.example.apitask.dtos.categories.CategorieRequestDTO;
import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.models.Categories;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CategoriesMapper {
    CategoriesResponseDTO toDTO(Categories categories);
    Categories toEntity(CategorieRequestDTO categoriesDTO);
    List<CategoriesResponseDTO> toListResponse(List<Categories> categories);
    Set<CategoriesResponseDTO> toSetResponse(Set<Categories> categories);
}
