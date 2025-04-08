data "aws_acm_certificate" "api_domain_cert" {
  domain      = var.api_gateway_domain_name
  statuses = ["ISSUED"]
  most_recent = true
}