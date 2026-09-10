-- ============================================================================
-- OECP - Platform Configuration Management
-- Migration: V1
--
-- Creates dedicated platform_configurations and platform_configuration_history
-- tables under cloud_platform database.
-- ============================================================================

-- 1. Create public.platform_configurations table
CREATE TABLE IF NOT EXISTS public.platform_configurations (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    configuration_name character varying(100) NOT NULL,
    configuration_category character varying(100) NOT NULL,
    description character varying(1000),
    status character varying(50) NOT NULL,
    effective_date date NOT NULL,

    -- Platform Settings
    platform_name character varying(150) NOT NULL,
    platform_url character varying NOT NULL,
    environment character varying(20) NOT NULL,
    default_language character varying NOT NULL,
    default_time_zone character varying NOT NULL,
    default_currency character varying NOT NULL,

    -- Operational Configuration
    maintenance_mode character varying(10) NOT NULL,
    feature_toggle character varying(10) NOT NULL,
    auto_backup character varying(10) NOT NULL,
    session_timeout integer NOT NULL,
    password_expiry integer NOT NULL,
    maximum_login_attempts integer NOT NULL,

    -- Audit & Soft Delete
    created_at date NOT NULL,
    created_by character varying(50) NOT NULL,
    updated_at date NOT NULL,
    updated_by character varying(50) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    deleted_at date,
    deleted_by character varying(50),
    version integer NOT NULL DEFAULT 1,

    -- Primary Key Constraint
    CONSTRAINT pk_platform_configurations PRIMARY KEY (id),

    -- Check Constraints
    CONSTRAINT chk_platform_config_environment CHECK (
        environment IN ('DEVELOPMENT', 'TESTING', 'STAGING', 'PRODUCTION')
    ),
    CONSTRAINT chk_platform_config_status CHECK (
        status IN ('ACTIVE', 'INACTIVE')
    ),
    CONSTRAINT chk_platform_config_maintenance_mode CHECK (
        maintenance_mode IN ('ENABLED', 'DISABLED')
    ),
    CONSTRAINT chk_platform_config_feature_toggle CHECK (
        feature_toggle IN ('ENABLED', 'DISABLED')
    ),
    CONSTRAINT chk_platform_config_auto_backup CHECK (
        auto_backup IN ('ENABLED', 'DISABLED')
    ),
    CONSTRAINT chk_platform_config_session_timeout CHECK (
        session_timeout > 0
    ),
    CONSTRAINT chk_platform_config_password_expiry CHECK (
        password_expiry > 0
    ),
    CONSTRAINT chk_platform_config_maximum_login_attempts CHECK (
        maximum_login_attempts > 0
    ),
    CONSTRAINT chk_platform_config_version CHECK (
        version >= 1
    )
);



-- 2. Create public.platform_configuration_history table
CREATE TABLE IF NOT EXISTS public.platform_configuration_history (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    configuration_id uuid NOT NULL,
    version integer NOT NULL,
    configuration_name character varying(100) NOT NULL,
    configuration_category character varying(100) NOT NULL,
    description character varying(1000),
    status character varying(50) NOT NULL,
    effective_date date NOT NULL,

    -- Platform Settings
    platform_name character varying(150) NOT NULL,
    platform_url character varying NOT NULL,
    environment character varying(20) NOT NULL,
    default_language character varying NOT NULL,
    default_time_zone character varying NOT NULL,
    default_currency character varying NOT NULL,

    -- Operational Configuration
    maintenance_mode character varying(10) NOT NULL,
    feature_toggle character varying(10) NOT NULL,
    auto_backup character varying(10) NOT NULL,
    session_timeout integer NOT NULL,
    password_expiry integer NOT NULL,
    maximum_login_attempts integer NOT NULL,

    -- Audit Metadata
    recorded_at date NOT NULL,
    recorded_by character varying(50) NOT NULL,
    change_reason character varying(100),

    -- Constraints
    CONSTRAINT pk_platform_configuration_history PRIMARY KEY (id),
    CONSTRAINT fk_platform_config_history_config FOREIGN KEY (configuration_id)
        REFERENCES public.platform_configurations (id) ON DELETE CASCADE
);

-- 1. Partial Unique Index: Guarantees unique configuration name among active records (BR-0012)
-- Prevents race conditions during concurrent insert/update operations.
CREATE UNIQUE INDEX IF NOT EXISTS uq_platform_config_active_name
ON public.platform_configurations (LOWER(configuration_name))
WHERE is_deleted = false;

-- 2. History Lookup Composite Index (configuration_id, version DESC)
-- Accelerates version lookup, history queries, and rollback operations (BR-0016)
CREATE INDEX IF NOT EXISTS idx_platform_config_history_lookup
ON public.platform_configuration_history (configuration_id, version DESC);

-- 3. Upgrade DATE columns to TIMESTAMPTZ to retain microsecond/millisecond precision
ALTER TABLE public.platform_configurations
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at::TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at::TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN deleted_at TYPE TIMESTAMP WITH TIME ZONE USING deleted_at::TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.platform_configuration_history
    ALTER COLUMN recorded_at TYPE TIMESTAMP WITH TIME ZONE USING recorded_at::TIMESTAMP WITH TIME ZONE;


