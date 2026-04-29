-- Baseline migration for KDS database.
-- Establishes Flyway tracking and creates a marker table so we can
-- verify migrations actually ran (vs silently no-op'ing).

CREATE TABLE flyway_baseline_marker (
                                        id           SMALLINT PRIMARY KEY DEFAULT 1,
                                        baselined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                        note         TEXT NOT NULL,
                                        CONSTRAINT flyway_baseline_marker_singleton CHECK (id = 1)
);

INSERT INTO flyway_baseline_marker (note)
VALUES ('Flyway baseline established. Real schema migrations follow.');