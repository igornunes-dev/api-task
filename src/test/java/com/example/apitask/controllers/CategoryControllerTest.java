package com.example.apitask.controllers;

import com.example.apitask.controllers.CategoryController;
import com.example.apitask.dtos.categories.CategorieRequestDTO;
import com.example.apitask.dtos.categories.CategoriesResponseDTO;
import com.example.apitask.infra.security.SecurityFilter;
import com.example.apitask.infra.security.TokenService;
import com.example.apitask.services.CategoryService;
import com.example.apitask.factories.CategoryFactory;

import com.example.apitask.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    private final CategoryFactory categoryFactory = new CategoryFactory();

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {
        var requestDTO = categoryFactory.createCategoryRequest(1);
        var responseDTO = categoryFactory.createCategoryResponse(1);

        when(categoryService.createCategory(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/categories/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDTO.id().toString()))
                .andExpect(jsonPath("$.name").value(responseDTO.name()));

        verify(categoryService, times(1)).createCategory(any());
    }

    @Test
    void shouldReturnAllCategories() throws Exception {
        var responseDTO1 = categoryFactory.createCategoryResponse(1);
        var responseDTO2 = categoryFactory.createCategoryResponse(2);
        List<CategoriesResponseDTO> categories = List.of(responseDTO1, responseDTO2);

        when(categoryService.findAllCategory()).thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDTO1.id().toString()))
                .andExpect(jsonPath("$[0].name").value(responseDTO1.name()))
                .andExpect(jsonPath("$[1].id").value(responseDTO2.id().toString()))
                .andExpect(jsonPath("$[1].name").value(responseDTO2.name()));

        verify(categoryService, times(1)).findAllCategory();
    }

    @Test
    void shouldDeleteCategorySuccessfully() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String successMessage = "Category deleted successfully";

        when(categoryService.deleteCategoryId(categoryId)).thenReturn(successMessage);

        mockMvc.perform(delete("/categories/category/{id}", categoryId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));

        verify(categoryService, times(1)).deleteCategoryId(categoryId);
    }
}
