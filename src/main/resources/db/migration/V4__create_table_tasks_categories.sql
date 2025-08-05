CREATE TABLE tasks_category (
    tasks_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (tasks_id, category_id),
    CONSTRAINT fk_tasks_category_tasks FOREIGN KEY (tasks_id) REFERENCES tasks (id),
    CONSTRAINT fk_tasks_category_category FOREIGN KEY (category_id) REFERENCES categories (id)
)


