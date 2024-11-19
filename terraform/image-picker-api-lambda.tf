resource "aws_lambda_function" "image_picker_api" {
  function_name = var.image_picker_api_lambda_name
  role          = aws_iam_role.image_picker_lambda_api.arn
  handler       = "not.used.in.provided.runtime"
  runtime       = "provided.al2"
  architectures = ["arm64"]
  timeout       = 120
  memory_size   = 2048

  ephemeral_storage {
    size = 512
  }

  s3_bucket = aws_s3_bucket.lambda_source_bucket.id
  s3_key    = var.image_picker_api_lambda_name
  source_code_hash = filebase64sha256("${path.module}/../image-picker-api/build/function.zip")

  depends_on = [aws_s3_object.image_picker_api_file_upload]

  environment {
    variables = tomap({
      AUTH_SERVER_URL           = var.auth_server_url
      OIDC_CLIENT_ID            = var.oidc_client_id
      OIDC_CLIENT_SECRET        = var.oidc_client_secret
      BUCKET_NAME               = var.image_picker_bucket_name
      MONGODB_CONNECTION_STRING = var.mongodb_connection_string
      MONGODB_DATABASE_NAME     = var.mongodb_database_name
      DISABLE_SIGNAL_HANDLERS   = "true"
      QUARKUS_HTTP_ROOT_PATH    = "/"
      IMAGE_PROCESSING_QUEUE_URL = aws_sqs_queue.image_processing_queue.id
    })
  }
}

resource "aws_s3_object" "image_picker_api_file_upload" {
  bucket        = aws_s3_bucket.lambda_source_bucket.bucket
  key           = var.image_picker_api_lambda_name
  force_destroy = true
  source        = "${path.module}/../image-picker-api/build/function.zip"
  etag = filebase64sha256("${path.module}/../image-picker-api/build/function.zip")

  depends_on = [aws_s3_bucket.lambda_source_bucket]
}

resource "aws_cloudwatch_log_group" "image-picker" {
  name              = "/aws/lambda/${aws_lambda_function.image_picker_api.function_name}"
  retention_in_days = 30
}

data "aws_iam_policy_document" "image_picker_api_assume_lambda_role" {
  statement {
    effect = "Allow"

    principals {
      type = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "image_picker_api_s3_policy_document" {
  statement {
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
      "s3:ListObjects",
      "s3:PutObjectTagging",
      "s3:GetObjectTagging"
    ]

    resources = [
      aws_s3_bucket.image-picker-images.arn,
      "${aws_s3_bucket.image-picker-images.arn}/*"
    ]
  }
}

resource "aws_iam_role" "image_picker_lambda_api" {
  name               = "${var.image_picker_api_lambda_name}-lambda"
  assume_role_policy = data.aws_iam_policy_document.image_picker_api_assume_lambda_role.json
}

resource "aws_iam_role_policy_attachment" "image_picker_api_basic_execution" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.image_picker_lambda_api.name
}

resource "aws_iam_policy" "image_picker_api_s3_policy" {
  name   = "image_picker_s3_policy"
  policy = data.aws_iam_policy_document.image_picker_api_s3_policy_document.json
}

resource "aws_iam_role_policy_attachment" "image_picker_api_s3_policy_attachment" {
  policy_arn = aws_iam_policy.image_picker_api_s3_policy.arn
  role       = aws_iam_role.image_picker_lambda_api.name
}

data "aws_iam_policy_document" "image_picker_sqs_policy_document" {
  statement {
    actions = ["sqs:GetQueueUrl", "sqs:SendMessage"]
    resources = [aws_sqs_queue.image_processing_queue.arn]
  }
}

resource "aws_iam_policy" "image_picker_sqs_policy" {
  policy = data.aws_iam_policy_document.image_picker_sqs_policy_document.json
  name   = "image_picker_sqs_policy"
}

resource "aws_iam_role_policy_attachment" "image_picker_sqs_policy_attachment" {
  policy_arn = aws_iam_policy.image_picker_sqs_policy.arn
  role = aws_iam_role.image_picker_lambda_api.name
}

resource "aws_lambda_permission" "image_picker_api_invoke_permission" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.image_picker_api.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_stage.image_picker_api.execution_arn}/*"
}