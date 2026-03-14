CREATE TABLE IF NOT EXISTS user_details (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(50),
    profile_status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

INSERT INTO user_details (
    created_at,
    updated_at,
    user_id,
    name,
    password,
    profile_status,
    role,
    username
)
VALUES (
           NOW(),
           NOW(),
           gen_random_uuid(),
           'Amardeep',
           '$2a$12$ntRlVru4hMYNZGAZRijvc.vGHokMmYWJEZ7XX3BV3Afh8IOBBIQOW',
           'ACTIVE',
           'ADMIN',
           'super'
       );