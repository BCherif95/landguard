-- LA BOUSSOLE — durable alert delivery ledger.
--
-- Alerts used to be dispatched from an in-memory ThreadPoolTaskExecutor with a
-- bounded queue, fired from inside the monitoring event's transaction. Two
-- consequences, both unacceptable for a product whose promise is to warn an
-- owner before the concrete sets:
--
--   1. every queued CRITICAL alert was lost, silently, on restart or crash —
--      no retry, no dead letter, no trace that anything had been dropped;
--   2. dispatch started BEFORE the transaction committed, so a rollback still
--      sent the SMS: an alert about an event that never existed.
--
-- This table is the transactional outbox that fixes both. A delivery intent is
-- committed atomically with the event that justifies it, then drained by a
-- worker that retries with exponential backoff and dead-letters what it cannot
-- deliver. Rows are never removed: an alert that failed to reach its recipient
-- is itself evidence when a dispatch is later contested before a court.

CREATE TABLE alert_deliveries (
    id                VARCHAR(36)  PRIMARY KEY,
    event_id          VARCHAR(36)  NOT NULL,
    recipient_user_id VARCHAR(36)  NOT NULL,
    channel           VARCHAR(16)  NOT NULL,
    -- Mirrors AlertChannel.dispatchPriority(): SMS=0, PUSH=1, EMAIL=2.
    -- Denormalised so the queue orders by urgency in SQL, on an index.
    dispatch_priority INT          NOT NULL,
    status            VARCHAR(16)  NOT NULL,
    attempt_count     INT          NOT NULL DEFAULT 0,
    next_attempt_at   DATETIME(6)  NOT NULL,
    last_error        VARCHAR(500),
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6)  NOT NULL,
    delivered_at      DATETIME(6),
    CONSTRAINT fk_alert_deliveries_event
        FOREIGN KEY (event_id) REFERENCES monitoring_events (id) ON DELETE CASCADE,
    CONSTRAINT fk_alert_deliveries_recipient
        FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_alert_deliveries_attempts CHECK (attempt_count >= 0)
);

-- One alert per recipient per channel per event: makes a duplicate enqueue —
-- a retried HTTP call, a replayed detection — structurally impossible rather
-- than merely unlikely. Sending the same owner the same warning twice erodes
-- exactly the trust the alert depends on.
CREATE UNIQUE INDEX ux_alert_deliveries_fanout
    ON alert_deliveries (event_id, recipient_user_id, channel);

-- The drain query: PENDING rows that are due, urgent channel first.
CREATE INDEX ix_alert_deliveries_queue
    ON alert_deliveries (status, dispatch_priority, next_attempt_at);

-- The stale-claim reaper, and the dead-letter console.
CREATE INDEX ix_alert_deliveries_status_updated
    ON alert_deliveries (status, updated_at);

CREATE INDEX ix_alert_deliveries_recipient
    ON alert_deliveries (recipient_user_id);
