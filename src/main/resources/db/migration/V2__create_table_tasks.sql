CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    date_conclusion TIMESTAMP WITHOUT TIME ZONE,
    date_expiration TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    users_id UUID NOT NULL,
    CONSTRAINT fk_tasks_on_users FOREIGN KEY (users_id) REFERENCES users (id)
)