-- Seed the single system-wide Global Settings record required by Team 4.

INSERT INTO platform_settings
(
    setting_name,
    category,
    description,
    status,

    default_language,
    default_time_zone,
    default_currency,
    date_format,
    time_format,
    number_format,

    session_timeout,
    auto_logout,
    password_expiry,
    maximum_login_attempts,
    maintenance_notification,
    system_announcement,

    multi_factor_authentication,
    email_notifications,
    sms_notifications,
    push_notifications,

    maximum_file_upload_size,
    default_theme,
    maintenance_mode,

    version_number,

    created_at,
    created_by,
    updated_at,
    updated_by
)
VALUES (
           'GLOBAL_SETTINGS',
           'GLOBAL',
           'System-wide global platform settings',
           'ACTIVE',

           'English',
           'Asia/Kolkata',
           'INR',
           'DD/MM/YYYY',
           '24 Hours',
           '#,##0.00',

           30,
           TRUE,
           90,
           5,
           FALSE,
           TRUE,

           TRUE,
           TRUE,
           TRUE,
           TRUE,

           100,
           'Light',
           FALSE,

           1,

           CURRENT_TIMESTAMP,
           'SYSTEM',
           CURRENT_TIMESTAMP,
           'SYSTEM'
       );
