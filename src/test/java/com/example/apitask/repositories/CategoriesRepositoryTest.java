package com.example.apitask.repositories;

import com.example.apitask.models.Categories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CategoriesRepositoryTest {

    @Test
    void contextLoads() {}

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Test
    void shouldReturnTrueWhenCategoryExistsByName() {
        Categories category = new Categories();
        category.setName("Category");
        categoriesRepository.saveAndFlush(category);

        boolean exists = categoriesRepository.existsByName("Category");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCategoryDoesNotExistsByName() {
        boolean exists = categoriesRepository.existsByName("Category");
        assertThat(exists).isFalse();
    }
}
