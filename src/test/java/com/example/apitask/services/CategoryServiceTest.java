package com.example.apitask.services;

import com.example.apitask.dtos.categories.CategorieRequestDTO;
import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.exceptions.DuplicateResourceException;
import com.example.apitask.exceptions.ResourceNotFoundException;
import com.example.apitask.factories.CategoryFactory;
import com.example.apitask.factories.UserFactory;
import com.example.apitask.mappers.CategoriesMapper;
import com.example.apitask.models.Categories;
import com.example.apitask.repositories.CategoriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    private CategoryService categoryService;

    @Mock
    private CategoriesRepository categoriesRepository;

    @Mock
    private CategoriesMapper categoriesMapper;

    private CategoryFactory categoriesFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryService(categoriesRepository, categoriesMapper);
        categoriesFactory = new CategoryFactory();
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        CategorieRequestDTO requestDTO = categoriesFactory.createCategoryRequest(1);
        Categories categoryEntity = categoriesFactory.createCategory(1);
        CategoriesResponseDTO responseDTO = categoriesFactory.createCategoryResponse(1);

        when(categoriesRepository.existsByName(requestDTO.name())).thenReturn(false);
        when(categoriesMapper.toEntity(requestDTO)).thenReturn(categoryEntity);
        when(categoriesMapper.toDTO(categoryEntity)).thenReturn(responseDTO);


        CategoriesResponseDTO result = categoryService.createCategory(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(categoriesRepository, times(1)).existsByName(requestDTO.name());
        verify(categoriesRepository, times(1)).save(categoryEntity);
        verify(categoriesMapper, times(1)).toEntity(requestDTO);
        verify(categoriesMapper, times(1)).toDTO(categoryEntity);
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        CategorieRequestDTO requestDTO = categoriesFactory.createCategoryRequest(1);
        when(categoriesRepository.existsByName(requestDTO.name())).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category already exists");

        verify(categoriesRepository, times(1)).existsByName(requestDTO.name());
        verifyNoMoreInteractions(categoriesRepository, categoriesMapper);
    }

    @Test
    void shouldReturnAllCategoriesSuccessfully() {
        List<Categories> categoryEntities = List.of(
                categoriesFactory.createCategory(1),
                categoriesFactory.createCategory(2)
        );

        List<CategoriesResponseDTO> responseDTOs = List.of(
                categoriesFactory.createCategoryResponse(1),
                categoriesFactory.createCategoryResponse(2)
        );

        when(categoriesRepository.findAll()).thenReturn(categoryEntities);
        when(categoriesMapper.toListResponse(categoryEntities)).thenReturn(responseDTOs);

        // Act
        List<CategoriesResponseDTO> result = categoryService.findAllCategory();

        // Assert
        assertThat(result).isEqualTo(responseDTOs);
        verify(categoriesRepository, times(1)).findAll();
        verify(categoriesMapper, times(1)).toListResponse(categoryEntities);
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesFound() {
        when(categoriesRepository.findAll()).thenReturn(List.of());
        when(categoriesMapper.toListResponse(List.of())).thenReturn(List.of());

        List<CategoriesResponseDTO> result = categoryService.findAllCategory();

        assertTrue(result.isEmpty());
        verify(categoriesRepository, times(1)).findAll();
        verify(categoriesMapper, times(1)).toListResponse(List.of());
    }

    @Test
    void shouldDeleteCategorySuccessfully() {
        // Arrange
        Categories category = categoriesFactory.createCategory(1);
        UUID categoryId = category.getId();

        when(categoriesRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoriesRepository).deleteById(categoryId);

        // Act
        String result = categoryService.deleteCategoryId(categoryId);

        // Assert
        assertThat(result).isEqualTo("Category deleted successfully");
        verify(categoriesRepository, times(1)).findById(categoryId);
        verify(categoriesRepository, times(1)).deleteById(categoryId);
        verifyNoMoreInteractions(categoriesRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        when(categoriesRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategoryId(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("category not found");

        verify(categoriesRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoriesRepository);
    }
}
