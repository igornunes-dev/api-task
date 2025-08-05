package com.example.apitask.controllers;

import com.example.apitask.dtos.categories.CategorieRequestDTO;
import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CategoriesResponseDTO> createCategory(@RequestBody CategorieRequestDTO categoriesDTO) {
        CategoriesResponseDTO categories = categoryService.createCategory(categoriesDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(categories);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CategoriesResponseDTO>> findAllCategory() {
        List<CategoriesResponseDTO> categoriesDTOS = categoryService.findAllCategory();
        return ResponseEntity.status(HttpStatus.OK).body(categoriesDTOS);
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteCategory(@PathVariable("id")UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.deleteCategoryId(id));
    }

}
