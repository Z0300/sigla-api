CREATE TABLE events
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(255)        NOT NULL,
    description  TEXT,
    venue        VARCHAR(255)        NOT NULL,
    start_date   DATETIME(6)         NOT NULL,
    end_date     DATETIME(6)         NOT NULL,
    capacity     INT UNSIGNED        NOT NULL,
    status       VARCHAR(20)         NOT NULL DEFAULT 'draft',
    organizer_id BIGINT UNSIGNED     NOT NULL,
    created_at   DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_events_user FOREIGN KEY (organizer_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_events_organizer_id (organizer_id),
    INDEX idx_events_start_date (start_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE sessions
(
    id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    event_id   BIGINT UNSIGNED NOT NULL,
    title      VARCHAR(255)    NOT NULL,
    room       VARCHAR(255)    NOT NULL,
    start_time DATETIME(6)     NOT NULL,
    end_time   DATETIME(6)     NOT NULL,
    capacity   INT UNSIGNED    NOT NULL,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_sessions_events FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    INDEX idx_sessions_event_id (event_id),
    INDEX idx_sessions_start_time (start_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE attendees
(
    id            BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED NOT NULL,
    event_id      BIGINT UNSIGNED NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'registered',
    registered_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_attendees_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_attendees_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT uk_attendees_user_event UNIQUE (user_id, event_id),
    INDEX idx_attendees_event_id (event_id),
    INDEX idx_attendees_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE qr_tokens
(
    id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    attendee_id BIGINT UNSIGNED NOT NULL,
    token       VARCHAR(255)    NOT NULL,
    type        VARCHAR(20)     NOT NULL,
    used        BOOLEAN         DEFAULT FALSE NOT NULL,
    scanned_at  DATETIME(6)     NULL,
    expires_at  DATETIME(6)     NOT NULL,
    created_at  DATETIME(6)     DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_qr_tokens_attendee FOREIGN KEY (attendee_id) REFERENCES attendees (id) ON DELETE CASCADE,
    INDEX idx_qr_tokens_token (token),
    INDEX idx_qr_tokens_attendee_id (attendee_id),
    INDEX idx_qr_tokens_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE session_qr
(
    id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT UNSIGNED NOT NULL,
    token      VARCHAR(255)    NOT NULL,
    active     BOOLEAN         DEFAULT TRUE NOT NULL,
    expires_at DATETIME(6)     NOT NULL,
    created_at DATETIME(6)     DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_session_qr_sessions FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,
    INDEX idx_session_qr_token (token),
    INDEX idx_session_qr_session_id (session_id),
    INDEX idx_session_qr_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE check_ins
(
    id            BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    attendee_id   BIGINT UNSIGNED NOT NULL,
    session_id    BIGINT UNSIGNED NOT NULL,
    qr_token_id   BIGINT UNSIGNED NULL,
    method        VARCHAR(20)     NOT NULL,
    checked_in_at DATETIME(6)     DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_check_ins_attendee FOREIGN KEY (attendee_id) REFERENCES attendees (id) ON DELETE CASCADE,
    CONSTRAINT fk_check_ins_session FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_check_ins_qr_token FOREIGN KEY (qr_token_id) REFERENCES qr_tokens (id) ON DELETE SET NULL,
    CONSTRAINT uk_check_ins_attendee_session UNIQUE (attendee_id, session_id),
    INDEX idx_check_ins_attendee_id (attendee_id),
    INDEX idx_check_ins_session_id (session_id),
    INDEX idx_check_ins_checked_in_at (checked_in_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;