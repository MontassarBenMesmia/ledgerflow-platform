variable "aws_region" {
  type    = string
  default = "eu-west-1"
}
variable "environment" {
  type    = string
  default = "portfolio"
}
variable "database_password" {
  type      = string
  sensitive = true
}
