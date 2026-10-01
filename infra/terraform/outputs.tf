output "database_endpoint" { value = aws_db_instance.ledger.address }
output "kafka_bootstrap_brokers" { value = aws_msk_serverless_cluster.events.arn }
