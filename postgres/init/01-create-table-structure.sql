-- === Create login role if missing and grant access ===
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'admin') THEN
    CREATE ROLE admin LOGIN PASSWORD 'admin';
  END IF;
END
$$;

-- Grants assume you're running this inside the "integration" database
GRANT CONNECT ON DATABASE integration TO admin;
GRANT USAGE ON SCHEMA public TO admin;

-- If tables already exist, grant now; otherwise defaults below will cover new ones
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;

-- Default privileges so future tables/sequences are accessible to admin
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLES TO admin;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO admin;

-- ======================================================================
-- Tables
-- ======================================================================

CREATE TABLE camel_messageprocessed (
  id               INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  processor_name   VARCHAR(255),
  message_id       VARCHAR(100),
  created_at       TIMESTAMP
);

-- This looks like an application-managed sequence table; kept as-is
CREATE TABLE camel_messageprocessed_seq (
  next_val       INTEGER NOT NULL,
  sequence_name  VARCHAR(45) NOT NULL,
  PRIMARY KEY (next_val)
);

CREATE TABLE component (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name               VARCHAR(100),
  type               VARCHAR(45),
  category           VARCHAR(45),
  route_id           INTEGER,
  inbound_state      VARCHAR(45),
  outbound_state     VARCHAR(45),
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE TABLE component_property (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  component_id       INTEGER,
  property_key       VARCHAR(100),
  value              VARCHAR(100),
  end_date           TIMESTAMP,
  created_date       TIMESTAMP,
  created_by_user_id VARCHAR(45)
);

CREATE TABLE message (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  content            TEXT,
  content_type       VARCHAR(45),
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE TABLE outbox_event (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_date_time    TIMESTAMP,
  message_flow_id    INTEGER,
  component_id       INTEGER,
  route_id           INTEGER,
  owner              VARCHAR(45),
  retry_count        INTEGER,
  retry_after        TIMESTAMP,
  error              TEXT,
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE INDEX idx_outbox_event_component ON outbox_event (component_id);

CREATE TABLE inbox_event (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_date_time    TIMESTAMP,
  message_flow_id    INTEGER,
  component_id       INTEGER,
  route_id           INTEGER,
  owner              VARCHAR(45),
  jms_message_id     VARCHAR(100),
  retry_count        INTEGER,
  retry_after        TIMESTAMP,
  error              TEXT,
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE INDEX idx_inbox_event_component ON inbox_event (component_id);

CREATE TABLE message_flow_group (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE TABLE message_flow (
  id                      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  component_id            INTEGER,
  group_id                INTEGER,
  message_id              INTEGER,
  parent_message_flow_id  INTEGER,
  action                  VARCHAR(100),
  created_by_user_id      VARCHAR(45),
  created_date            TIMESTAMP
);

CREATE INDEX idx_message_flow_parent ON message_flow (parent_message_flow_id);

CREATE TABLE message_flow_filtered (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  message_flow_id    INTEGER,
  name               VARCHAR(45),
  reason             VARCHAR(100),
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE TABLE message_flow_error (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  message_flow_id    INTEGER,
  details            TEXT,
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE TABLE route (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name               VARCHAR(45) UNIQUE,
  owner              VARCHAR(45),
  created_by_user_id VARCHAR(45),
  created_date       TIMESTAMP
);

CREATE TABLE shedlock (
  name       VARCHAR(64) PRIMARY KEY,
  lock_until TIMESTAMP(3),
  locked_at  TIMESTAMP(3),
  locked_by  VARCHAR(255)
);

CREATE TABLE message_flow_property (
  id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  property_key       VARCHAR(100),
  value              VARCHAR(100),
  message_flow_id    INTEGER,
  created_date       TIMESTAMP,
  created_by_user_id VARCHAR(45)
);

-- Seed data equivalent
INSERT INTO camel_messageprocessed_seq (sequence_name, next_val)
VALUES ('default', 1)
ON CONFLICT DO NOTHING; -- safe if re-run
