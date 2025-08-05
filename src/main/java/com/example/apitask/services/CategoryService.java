package com.example.apitask.services;

import com.example.apitask.dtos.categories.CategorieRequestDTO;
import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.exceptions.DuplicateResourceException;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.mappers.CategoriesMapper;
import com.example.apitask.models.Categories;
import com.example.apitask.repositories.CategoriesRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoriesRepository categoriesRepository;
    private final CategoriesMapper categoriesMapper;

    public CategoryService(CategoriesRepository categoriesRepository, CategoriesMapper categoriesMapper) {
        this.categoriesRepository = categoriesRepository;
        this.categoriesMapper = categoriesMapper;
    }


    public CategoriesResponseDTO createCategory(@Valid CategorieRequestDTO categoriesDTO) {
        if(categoriesRepository.existsByName(categoriesDTO.name())) {
            throw new DuplicateResourceException("Category already exists");
        }
        Categories categories = categoriesMapper.toEntity(categoriesDTO);
        categoriesRepository.save(categories);
        return categoriesMapper.toDTO(categories);
    }

    public List<CategoriesResponseDTO> findAllCategory() {
        List<Categories> categoriesList = categoriesRepository.findAll();
        return categoriesMapper.toListResponse(categoriesList);
    }

    public String deleteCategoryId(UUID id) {
        categoriesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("category not found"));
        categoriesRepository.deleteById(id);
        return "Category deleted successfully";
    }
}
