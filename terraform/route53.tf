resource "aws_route53_record" "api_record" {
  name    = ""
  type    = "A"
  zone_id = data.aws_route53_zone.api_zone.zone_id

  alias {
    evaluate_target_health = true
    name                   = aws_api_gateway_domain_name.image_picker_api_domain.regional_domain_name
    zone_id                = aws_api_gateway_domain_name.image_picker_api_domain.regional_zone_id
  }
}

data "aws_route53_zone" "api_zone" {
  name = var.api_gateway_domain_name
}