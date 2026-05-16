-- =============================================================
-- Chimi V1 — schema inicial (MySQL 8.x)
-- Charset utf8mb4, motor InnoDB.
-- Timestamps almacenados en UTC (Hibernate jdbc.time_zone=UTC).
-- =============================================================

-- ---------- TENANTS ----------
CREATE TABLE tenants (
    id                          BIGINT       NOT NULL AUTO_INCREMENT,
    slug                        VARCHAR(60)  NOT NULL,
    name                        VARCHAR(200) NOT NULL,
    timezone                    VARCHAR(60)  NOT NULL DEFAULT 'America/Argentina/Buenos_Aires',
    wpp_phone_number_id         VARCHAR(60)  NULL,
    wpp_business_account_id     VARCHAR(60)  NULL,
    parallel_slots              INT          NOT NULL DEFAULT 1,
    min_lead_minutes            INT          NOT NULL DEFAULT 60,
    max_lead_days               INT          NOT NULL DEFAULT 60,
    slot_granularity_minutes    INT          NOT NULL DEFAULT 15,
    allow_cancel_by_wpp         TINYINT(1)   NOT NULL DEFAULT 1,
    allow_reschedule_by_wpp     TINYINT(1)   NOT NULL DEFAULT 1,
    active                      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at                  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenants_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- WORKING_HOURS ----------
CREATE TABLE working_hours (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT       NOT NULL,
    day_of_week VARCHAR(10)  NOT NULL,
    start_time  TIME         NOT NULL,
    end_time    TIME         NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_wh_tenant (tenant_id),
    CONSTRAINT fk_wh_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- BRANDING ----------
CREATE TABLE branding (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT       NOT NULL,
    display_name    VARCHAR(200) NOT NULL,
    logo_url        VARCHAR(500) NULL,
    primary_color   VARCHAR(9)   NULL DEFAULT '#2C7A7B',
    secondary_color VARCHAR(9)   NULL DEFAULT '#F6AD55',
    greeting        VARCHAR(1000) NULL,
    tone            VARCHAR(20)  NULL DEFAULT 'informal',
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_branding_tenant (tenant_id),
    CONSTRAINT fk_branding_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- APP_USERS ----------
CREATE TABLE app_users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id     BIGINT       NOT NULL,
    email         VARCHAR(200) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    full_name     VARCHAR(200) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'STAFF',
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_tenant_email (tenant_id, email),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- TUTORS ----------
CREATE TABLE tutors (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id  BIGINT       NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(30)  NOT NULL,
    email      VARCHAR(200) NULL,
    notes      VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_tutors_tenant_phone (tenant_id, phone),
    CONSTRAINT fk_tutors_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- PETS ----------
CREATE TABLE pets (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id  BIGINT       NOT NULL,
    tutor_id   BIGINT       NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NULL,
    species    VARCHAR(50)  NULL,
    breed      VARCHAR(100) NULL,
    notes      VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_pets_tenant (tenant_id),
    KEY ix_pets_tutor  (tutor_id),
    CONSTRAINT fk_pets_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_pets_tutor  FOREIGN KEY (tutor_id)  REFERENCES tutors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- APPOINTMENT_TYPES ----------
CREATE TABLE appointment_types (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id        BIGINT       NOT NULL,
    code             VARCHAR(50)  NOT NULL,
    name             VARCHAR(100) NOT NULL,
    duration_minutes INT          NOT NULL,
    description      VARCHAR(500) NULL,
    active           TINYINT(1)   NOT NULL DEFAULT 1,
    display_order    INT          NOT NULL DEFAULT 0,
    default_price    DECIMAL(12,2) NULL,
    created_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_apt_types_tenant_code (tenant_id, code),
    CONSTRAINT fk_apt_types_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- APPOINTMENTS ----------
CREATE TABLE appointments (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id           BIGINT       NOT NULL,
    appointment_type_id BIGINT       NOT NULL,
    pet_id              BIGINT       NOT NULL,
    tutor_id            BIGINT       NOT NULL,
    start_at            TIMESTAMP(6) NOT NULL,
    end_at              TIMESTAMP(6) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    source              VARCHAR(20)  NOT NULL DEFAULT 'DASHBOARD',
    notes               VARCHAR(2000) NULL,
    prepaid             TINYINT(1)   NOT NULL DEFAULT 0,
    prepaid_amount      DECIMAL(12,2) NULL,
    reminded_at         TIMESTAMP(6) NULL,
    cancelled_at        TIMESTAMP(6) NULL,
    cancel_reason       VARCHAR(500) NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_appts_tenant_start  (tenant_id, start_at),
    KEY ix_appts_tenant_status (tenant_id, status),
    CONSTRAINT fk_appts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_appts_type   FOREIGN KEY (appointment_type_id) REFERENCES appointment_types(id),
    CONSTRAINT fk_appts_pet    FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_appts_tutor  FOREIGN KEY (tutor_id) REFERENCES tutors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- SCHEDULE_BLOCKS ----------
CREATE TABLE schedule_blocks (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id           BIGINT       NOT NULL,
    start_at            TIMESTAMP(6) NOT NULL,
    end_at              TIMESTAMP(6) NOT NULL,
    reason              VARCHAR(500) NULL,
    created_by_user_id  BIGINT       NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_blocks_tenant_start (tenant_id, start_at),
    CONSTRAINT fk_blocks_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- FAQS ----------
CREATE TABLE faqs (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    tenant_id     BIGINT        NOT NULL,
    category      VARCHAR(20)   NOT NULL,
    question      VARCHAR(500)  NOT NULL,
    answer        VARCHAR(4000) NOT NULL,
    display_order INT           NOT NULL DEFAULT 0,
    active        TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_faqs_tenant_category (tenant_id, category),
    CONSTRAINT fk_faqs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- CONVERSATIONS ----------
CREATE TABLE conversations (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT       NOT NULL,
    phone           VARCHAR(30)  NOT NULL,
    state           VARCHAR(50)  NOT NULL DEFAULT 'IDLE',
    context_json    LONGTEXT     NULL,
    last_message_at TIMESTAMP(6) NULL,
    human_handoff   TINYINT(1)   NOT NULL DEFAULT 0,
    handoff_at      TIMESTAMP(6) NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_conv_tenant_phone (tenant_id, phone),
    CONSTRAINT fk_conv_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- WPP_MESSAGES ----------
CREATE TABLE wpp_messages (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT       NOT NULL,
    conversation_id BIGINT       NOT NULL,
    direction       VARCHAR(10)  NOT NULL,
    wpp_message_id  VARCHAR(100) NULL,
    type            VARCHAR(30)  NULL,
    body            LONGTEXT     NULL,
    sent_at         TIMESTAMP(6) NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_msgs_conv_ts (conversation_id, sent_at),
    CONSTRAINT fk_msgs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_msgs_conv   FOREIGN KEY (conversation_id) REFERENCES conversations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
