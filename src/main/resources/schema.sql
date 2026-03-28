-- ==========================================
-- Urimaigal Database Schema
-- ==========================================

-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS lawyer_reviews;
DROP TABLE IF EXISTS consultations;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS lawyers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS profile_analytics;

-- Users table
CREATE TABLE users (
    id           VARCHAR(36)  NOT NULL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    phone        VARCHAR(20),
    avatar       VARCHAR(500),
    role         VARCHAR(20)  NOT NULL DEFAULT 'client',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
);

-- Lawyers table
CREATE TABLE lawyers (
    id                  VARCHAR(36)    NOT NULL PRIMARY KEY,
    name                VARCHAR(100)   NOT NULL,
    avatar              VARCHAR(500),
    specialization      VARCHAR(50)    NOT NULL,
    experience          INT            NOT NULL DEFAULT 0,
    rating              DECIMAL(3, 1)  NOT NULL DEFAULT 0.0,
    review_count        INT            NOT NULL DEFAULT 0,
    location            VARCHAR(100)   NOT NULL,
    languages           TEXT           NOT NULL COMMENT 'comma-separated list',
    consultation_fee    DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    bio                 TEXT,
    available           TINYINT(1)     NOT NULL DEFAULT 1,
    badge               VARCHAR(20)    COMMENT 'Top Rated | Rising Star | Pro Bono',
    created_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_lawyers_specialization (specialization),
    INDEX idx_lawyers_available (available),
    INDEX idx_lawyers_rating (rating)
);

-- Consultations (bookings) table
CREATE TABLE consultations (
    id              VARCHAR(36)    NOT NULL PRIMARY KEY,
    client_id       VARCHAR(36)    NOT NULL,
    advocate_id     VARCHAR(36)    NOT NULL,
    lawyer_name     VARCHAR(100)   NOT NULL,
    consultation_date DATE         NOT NULL,
    consultation_time VARCHAR(10)  NOT NULL,
    mode            VARCHAR(10)    NOT NULL COMMENT 'chat | video',
    status          VARCHAR(20)    NOT NULL DEFAULT 'scheduled' COMMENT 'scheduled | completed | cancelled',
    fee             DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    notes           TEXT,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id)   REFERENCES users(id)   ON DELETE CASCADE,
    FOREIGN KEY (advocate_id) REFERENCES users(id)   ON DELETE RESTRICT,
    INDEX idx_consultations_client  (client_id),
    INDEX idx_consultations_advocate (advocate_id),
    INDEX idx_consultations_status  (status),
    INDEX idx_consultations_date    (consultation_date)
);

-- Chat messages table
CREATE TABLE chat_messages (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    role        VARCHAR(10)  NOT NULL COMMENT 'user | bot',
    content     TEXT         NOT NULL,
    suggestions TEXT         COMMENT 'JSON array of suggestion strings',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_messages_user (user_id),
    INDEX idx_chat_messages_created (created_at)
);

-- Lawyer reviews table
CREATE TABLE lawyer_reviews (
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    lawyer_id   VARCHAR(36) NOT NULL,
    user_id     VARCHAR(36) NOT NULL,
    rating      INT         NOT NULL,
    comment     TEXT,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lawyer_id) REFERENCES lawyers(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    UNIQUE KEY uq_review_user_lawyer (user_id, lawyer_id),
    INDEX idx_reviews_lawyer (lawyer_id)
);

-- Profile analytics table
CREATE TABLE profile_analytics (
    id              VARCHAR(36)    NOT NULL PRIMARY KEY,
    advocate_id     VARCHAR(36)    NOT NULL,
    profile_views   INT            NOT NULL DEFAULT 0,
    consultation_requests INT      NOT NULL DEFAULT 0,
    completed_consultations INT    NOT NULL DEFAULT 0,
    average_rating  DECIMAL(3, 2)  DEFAULT NULL,
    total_earnings  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    month_year      VARCHAR(7)     NOT NULL COMMENT 'YYYY-MM format',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (advocate_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_analytics_advocate_month (advocate_id, month_year),
    INDEX idx_analytics_advocate (advocate_id),
    INDEX idx_analytics_month (month_year)
);
