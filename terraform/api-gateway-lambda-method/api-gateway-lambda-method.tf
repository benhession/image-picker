resource "aws_api_gateway_method" "this" {
  authorization = "NONE"
  http_method   = var.http_method
  resource_id   = var.resource_id
  rest_api_id   = var.rest_api_id
}

resource "aws_api_gateway_integration" "this" {
  rest_api_id             = var.rest_api_id
  http_method             = var.http_method
  integration_http_method = "POST"
  resource_id             = var.resource_id
  type                    = "AWS_PROXY"
  uri                     = var.uri
}