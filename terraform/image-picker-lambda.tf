resource "aws_lambda_function" "image-picker" {
  function_name = var.image_picker_lambda_name
  role          = aws_iam_role.image_picker_lambda.arn
  handler       = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler"
  runtime       = "java17"
  architectures = ["arm64"]

  s3_bucket        = aws_s3_bucket.lambda_source_bucket.id
  s3_key           = var.image_picker_lambda_name
  source_code_hash = filebase64sha256("${path.module}/../build/function.zip")

  depends_on = [aws_s3_object.file_upload]

  environment {
    variables = tomap({
      AUTH_SERVER_URL    = var.auth_server_url
      OIDC_CLIENT_ID     = var.oidc_client_id
      OIDC_CLIENT_SECRET = var.oidc_client_secret
      BUCKET_NAME        = var.image_picker_bucket_name
    })
  }
}

data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "image_picker_lambda" {
  name               = "${var.image_picker_lambda_name}-lambda"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_role_policy_attachment" "basic" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.image_picker_lambda.name
}

resource "aws_lambda_permission" "this" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.image-picker.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_stage.image_picker_api.execution_arn}/*"
}

resource "aws_s3_object" "file_upload" {
  bucket        = aws_s3_bucket.lambda_source_bucket.bucket
  key           = var.image_picker_lambda_name
  force_destroy = true
  source        = "${path.module}/../build/function.zip"
  etag          = filebase64sha256("${path.module}/../build/function.zip")

  depends_on = [aws_s3_bucket.lambda_source_bucket]
}

resource "aws_cloudwatch_log_group" "image-picker" {
  name              = "/aws/lambda/${aws_lambda_function.image-picker.function_name}"
  retention_in_days = 30
}