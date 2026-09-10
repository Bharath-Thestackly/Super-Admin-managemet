-- Create platform_settings_history table with constraints and default values

CREATE TABLE platform_settings_history
(

    history_id                  UUID PRIMARY KEY    DEFAULT gen_random_uuid(),

    setting_id                  UUID         NOT NULL,

    version_number              BIGINT       NOT NULL,

    action                      VARCHAR(30)  NOT NULL,

    activity_status             VARCHAR(30)  NOT NULL,

    setting_name                VARCHAR(100) NOT NULL,

    category                    VARCHAR(100) NOT NULL,

    description                 VARCHAR(1000),

    status                      VARCHAR(30)  NOT NULL,

    default_language            VARCHAR(50)  NOT NULL,

    default_time_zone           VARCHAR(100) NOT NULL,

    default_currency            VARCHAR(20)  NOT NULL,

    date_format                 VARCHAR(50)  NOT NULL,

    time_format                 VARCHAR(30)  NOT NULL,

    number_format               VARCHAR(50)  NOT NULL,

    session_timeout             INTEGER      NOT NULL,

    auto_logout                 BOOLEAN      NOT NULL,

    password_expiry             INTEGER      NOT NULL,

    maximum_login_attempts      INTEGER      NOT NULL,

    maintenance_notification    BOOLEAN      NOT NULL,

    system_announcement         BOOLEAN      NOT NULL,

    multi_factor_authentication BOOLEAN      NOT NULL,

    email_notifications         BOOLEAN      NOT NULL,

    sms_notifications           BOOLEAN      NOT NULL,

    push_notifications          BOOLEAN      NOT NULL,

    maximum_file_upload_size    BIGINT       NOT NULL,

    default_theme               VARCHAR(30)  NOT NULL,

    maintenance_mode            BOOLEAN      NOT NULL,

    user_id                     VARCHAR(100) NOT NULL,

    user_name                   VARCHAR(100) NOT NULL,

    ip_address                  VARCHAR(45),

    changed_at                  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_platform_settings_history_setting
        FOREIGN KEY (setting_id)
            REFERENCES platform_settings (id),

    CONSTRAINT uk_setting_history_version
        UNIQUE (setting_id, version_number)
);

CREATE INDEX idx_platform_settings_history_setting_id
    ON platform_settings_history (setting_id);

CREATE INDEX idx_platform_settings_history_changed_at
    ON platform_settings_history (changed_at);