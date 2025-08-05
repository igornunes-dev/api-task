package com.example.apitask.seed;

import com.example.apitask.models.Categories;
import com.example.apitask.models.Tasks;
import com.example.apitask.repositories.CategoriesRepository;
import com.example.apitask.repositories.TasksRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements ApplicationRunner {

    private final CategoriesRepository categoriesRepository;

    public DatabaseSeeder(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(categoriesRepository.count() == 0) {
            Categories categories1 = new Categories("Personal");
            Categories categories2 = new Categories("Work");
            Categories categories3 = new Categories("Faculty");
            categoriesRepository.save(categories1);
            categoriesRepository.save(categories2);
            categoriesRepository.save(categories3);
        }
    }
}
