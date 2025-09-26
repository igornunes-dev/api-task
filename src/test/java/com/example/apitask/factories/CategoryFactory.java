package com.example.apitask.factories;

import com.example.apitask.dtos.categories.CategorieRequestDTO;
import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.models.Categories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class CategoryFactory {
    public Categories createCategory() {
        return createCategory(0);
    }

    public Categories createCategory(int number) {
        Categories category = new Categories();
        String uuidSeed = "category-" + number;
        category.setId(UUID.nameUUIDFromBytes(uuidSeed.getBytes()));
        category.setName("Category " + number);
        category.setTasks(new HashSet<>());
        return category;
    }

    public List<Categories> createCategoryList(int size) {
        List<Categories> categories = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            categories.add(createCategory(i));
        }
        return categories;
    }

    public CategorieRequestDTO createCategoryRequest(int number) {
        return new CategorieRequestDTO("Category " + number);
    }

    public CategoriesResponseDTO createCategoryResponse(int number) {
        String uuidSeed = "category-" + number;
        UUID id = UUID.nameUUIDFromBytes(uuidSeed.getBytes());
        return new CategoriesResponseDTO(
                id,
                "Category " + number
        );
    }

    public List<CategoriesResponseDTO> createCategoryResponseList(int size) {
        List<CategoriesResponseDTO> responses = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            responses.add(createCategoryResponse(i));
        }
        return responses;
    }
}
