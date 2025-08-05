CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    pointers INTEGER NOT NULL DEFAULT 0,
    streak_data DATE,
    role VARCHAR(255) NOT NULL
)