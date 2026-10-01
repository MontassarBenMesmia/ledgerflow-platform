CREATE TABLE transfer_views (
    id UUID PRIMARY KEY,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    initiated_by VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    reconciliation_status VARCHAR(32) NOT NULL
);

CREATE INDEX idx_transfer_views_occurred ON transfer_views(occurred_at DESC);

CREATE TABLE processed_events (
    id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);
