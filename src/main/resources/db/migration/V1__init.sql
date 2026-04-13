CREATE TABLE users
(
    id                BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    email             VARCHAR(255) UNIQUE NOT NULL,
    password_hash     VARCHAR(255),
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    is_active         BOOLEAN   DEFAULT TRUE,
    is_email_verified BOOLEAN   DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles
(
    id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE permissions
(
    id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE user_roles
(
    user_id BIGINT UNSIGNED,
    role_id BIGINT UNSIGNED,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE role_permissions
(
    role_id       BIGINT UNSIGNED,
    permission_id BIGINT UNSIGNED,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles (id),
    FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE social_accounts
(
    id               BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT UNSIGNED NOT NULL,
    provider         VARCHAR(50)     NOT NULL,
    provider_user_id VARCHAR(255)    NOT NULL,
    access_token     TEXT,
    refresh_token    TEXT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);