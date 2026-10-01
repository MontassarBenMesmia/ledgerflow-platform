# Operations runbook

## Local startup

Copy `.env.example` to `.env`, replace placeholder values, and run `docker compose up --build -d`. Open <http://localhost> and sign in with `operator` / `operator123`. This account exists only in the local imported realm.

Run the end-to-end check with `pwsh tests/smoke.ps1`. Start telemetry with `docker compose --profile observability up -d`; Grafana is available at <http://localhost:3000>.

## Failure recovery

- If Kafka is unavailable, committed outbox rows remain unpublished and are retried after recovery.
- If the projection is rebuilt, reset its consumer group and projection tables; immutable ledger records remain authoritative.
- A `REVIEW_REQUIRED` item indicates an event whose debit and credit fields disagree. Pause downstream settlement and investigate the source event and trace.
- Back up both databases before schema upgrades. Flyway migrations are forward-only; rollback means restoring a known backup and prior application image.

## Signals

Monitor HTTP error and latency rates, unpublished outbox age, Kafka consumer lag, projection failures, JVM saturation, and database connections. Trace IDs correlate gateway requests, ledger commits, and asynchronous consumers in Tempo.
