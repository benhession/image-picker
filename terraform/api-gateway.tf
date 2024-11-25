resource "aws_api_gateway_rest_api" "image_picker_api" {
  name               = "image_picker_api"
  binary_media_types = ["multipart/form-data"]
}

resource "aws_api_gateway_deployment" "image_picker" {
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
  depends_on = [
    module.post_image,
    module.get_all_images,
    module.get_image,
  ]

  variables = {
    deployed_at = timestamp()
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "image_picker_api" {
  deployment_id = aws_api_gateway_deployment.image_picker.id
  rest_api_id   = aws_api_gateway_rest_api.image_picker_api.id
  stage_name    = "image-picker-default"

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.canopy_rest_api.arn
    format          = jsonencode({
      "requestId"               = "$context.requestId", "sourceIp" = "$context.identity.sourceIp",
      "requestTime"             = "$context.requestTime", "protocol" = "$context.protocol",
      "httpMethod"              = "$context.httpMethod", "path" = "$context.path",
      "resourcePath"            = "$context.resourcePath", "routeKey" = "$context.routeKey",
      "status"                  = "$context.status", "responseLength" = "$context.responseLength",
      "integrationErrorMessage" = "$context.integrationErrorMessage"
    })
  }
}

resource "aws_api_gateway_method_settings" "canopy_rest_api" {
  method_path = "*/*"
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
  stage_name  = aws_api_gateway_stage.image_picker_api.stage_name
  depends_on  = [aws_api_gateway_account.gateway_account]

  settings {
    metrics_enabled = true
    logging_level   = "INFO"
  }
}

resource "aws_api_gateway_account" "gateway_account" {
  cloudwatch_role_arn = aws_iam_role.gateway_role.arn
  depends_on          = [aws_iam_role.gateway_role, aws_iam_role_policy.gateway_policy]
}

resource "aws_iam_role" "gateway_role" {
  name = "api_gateway_cloudwatch_logging"

  assume_role_policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Sid" : "",
        "Effect" : "Allow",
        "Principal" : {
          "Service" : [
            "apigateway.amazonaws.com"
          ]
        },
        "Action" : [
          "sts:AssumeRole"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy" "gateway_policy" {
  name       = "cloudwatch_logs_allow_policy"
  role       = aws_iam_role.gateway_role.id
  depends_on = [aws_iam_role.gateway_role]

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:DescribeLogGroups",
          "logs:DescribeLogStreams",
          "logs:PutLogEvents",
          "logs:GetLogEvents",
          "logs:FilterLogEvents"
        ],
        "Resource" : "*"
      }
    ]
  })
}

resource "aws_cloudwatch_log_group" "canopy_rest_api" {
  name              = "/aws/api_rest_gw/${aws_api_gateway_rest_api.image_picker_api.name}"
  retention_in_days = 30
}

resource "aws_api_gateway_resource" "image_resource" {
  path_part   = "image"
  parent_id   = aws_api_gateway_rest_api.image_picker_api.root_resource_id
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
}

resource "aws_api_gateway_resource" "image_by_id_resource" {
  parent_id   = aws_api_gateway_resource.image_resource.id
  path_part   = "{id}"
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
}

module "get_all_images" {
  source      = "./api-gateway-lambda-method"
  http_method = "GET"
  resource_id = aws_api_gateway_resource.image_resource.id
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
  uri         = aws_lambda_function.image_picker_api.invoke_arn
}


module "post_image" {
  source      = "./api-gateway-lambda-method"
  http_method = "POST"
  resource_id = aws_api_gateway_resource.image_resource.id
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
  uri         = aws_lambda_function.image_picker_api.invoke_arn
}

module "get_image" {
  source      = "./api-gateway-lambda-method"
  http_method = "GET"
  resource_id = aws_api_gateway_resource.image_by_id_resource.id
  rest_api_id = aws_api_gateway_rest_api.image_picker_api.id
  uri         = aws_lambda_function.image_picker_api.invoke_arn
}