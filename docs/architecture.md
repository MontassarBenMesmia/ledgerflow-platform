# Architecture decisions

## Financial correctness

Every transfer locks accounts in deterministic UUID order, validates currency and funds, changes both balances, creates one debit plus one credit, and writes an outbox record in one PostgreSQL transaction. Optimistic version columns prevent unnoticed overwrites. An idempotency key returns the original result for retries, including requests that wait behind the same account locks.

## Reliable event delivery

The transactional outbox closes the database/message-broker dual-write gap. A scheduled publisher sends unpublished records with Kafka's idempotent producer. The projection stores processed event IDs, so duplicate delivery does not duplicate a read model. The reconciliation rule independently checks debit/credit equality before labeling a projection `BALANCED`.

## Trust boundaries

Keycloak issues OAuth 2.0/OIDC tokens. Services validate the signature and issuer and map realm roles to Spring authorities. Nginx is the single browser-facing gateway. Redis enforces per-principal transfer limits. No application secret is embedded in an image.

## Production target

The Terraform reference maps the platform to private AWS networking, encrypted RDS PostgreSQL, MSK Serverless, and ElastiCache Serverless. The Helm chart applies non-root workloads, resource limits, probes, and declarative GitOps delivery through Argo CD. Production identity and secrets should use a managed identity provider and AWS Secrets Manager/External Secrets.
