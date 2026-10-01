data "aws_availability_zones" "available" { state = "available" }

resource "aws_vpc" "main" {
  cidr_block           = "10.40.0.0/16"
  enable_dns_hostnames = true
  tags                 = { Name = "ledgerflow-${var.environment}" }
}
resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 8, count.index + 10)
  availability_zone = data.aws_availability_zones.available.names[count.index]
}
resource "aws_security_group" "database" {
  name   = "ledgerflow-database"
  vpc_id = aws_vpc.main.id
}
resource "aws_db_subnet_group" "main" {
  name       = "ledgerflow-${var.environment}"
  subnet_ids = aws_subnet.private[*].id
}
resource "aws_db_instance" "ledger" {
  identifier             = "ledgerflow-${var.environment}"
  engine                 = "postgres"
  engine_version         = "17.4"
  instance_class         = "db.t4g.micro"
  allocated_storage      = 20
  db_name                = "ledger"
  username               = "ledgerflow"
  password               = var.database_password
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.database.id]
  storage_encrypted      = true
  multi_az               = false
  publicly_accessible    = false
  skip_final_snapshot    = true
}
resource "aws_msk_serverless_cluster" "events" {
  cluster_name = "ledgerflow-${var.environment}"
  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.database.id]
  }
  client_authentication {
    sasl {
      iam {
        enabled = true
      }
    }
  }
}
resource "aws_elasticache_serverless_cache" "rate_limit" {
  engine = "redis"
  name   = "ledgerflow-${var.environment}"
  cache_usage_limits {
    data_storage {
      maximum = 1
      unit    = "GB"
    }
    ecpu_per_second {
      maximum = 1000
    }
  }
  daily_snapshot_time = "03:00"
  security_group_ids  = [aws_security_group.database.id]
  subnet_ids          = aws_subnet.private[*].id
}
