resource "aws_lambda_function" "image_processor" {
  function_name = var.image_processor_lambda_name
  role          = aws_iam_role.image_processor.arn
  handler       = "not.used.in.provided.runtime"
  runtime       = "provided.al2"
  architectures = ["arm64"]
  timeout       = 120
  memory_size   = 2048

  ephemeral_storage {
    size = 512
  }

  s3_bucket = aws_s3_bucket.lambda_source_bucket.id
  s3_key    = var.image_processor_lambda_name
  source_code_hash = filebase64sha256("${path.module}/../image-processor/build/function.zip")

  depends_on = [aws_s3_object.image_processor_file_upload]

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
    })
  }
}

resource "aws_s3_object" "image_processor_file_upload" {
  bucket        = aws_s3_bucket.lambda_source_bucket.bucket
  key           = var.image_processor_lambda_name
  force_destroy = true
  source        = "${path.module}/../image-processor/build/function.zip"
  etag = filebase64sha256("${path.module}/../image-processor/build/function.zip")

  depends_on = [aws_s3_bucket.lambda_source_bucket]
}

resource "aws_cloudwatch_log_group" "image_processor_log_group" {
  name              = "/aws/lambda/${aws_lambda_function.image_processor.function_name}"
  retention_in_days = 30
}

data "aws_iam_policy_document" "image_processor_assume_lambda_role" {
  statement {
    effect = "Allow"

    principals {
      type = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "image_processor" {
  name               = "${var.image_processor_lambda_name}-lambda"
  assume_role_policy = data.aws_iam_policy_document.image_processor_assume_lambda_role.json
}

data "aws_iam_policy_document" "image_processor_s3_policy_document" {
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

resource "aws_iam_policy" "image_processor_s3_policy" {
  name   = "image_processor_s3_policy"
  policy = data.aws_iam_policy_document.image_processor_s3_policy_document.json
}

resource "aws_iam_role_policy_attachment" "image_processor_s3_attachment" {
  role = aws_iam_role.image_processor.name
  policy_arn = aws_iam_policy.image_processor_s3_policy.arn
}

resource "aws_iam_role_policy_attachment" "image_processor_basic_execution" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.image_processor.name
}

data "aws_iam_policy_document" "image_processor_sqs_policy_document" {
  statement {
    actions   = ["sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
    resources = [aws_sqs_queue.image_processing_queue.arn]
    effect = "Allow"
  }
}

resource "aws_iam_policy" "image_processor_sqs_policy" {
  name   = "image_processor_sqs_policy"
  policy = data.aws_iam_policy_document.image_processor_sqs_policy_document.json
}

resource "aws_iam_role_policy_attachment" "image_processor_sqs_policy_attachment" {
  policy_arn = aws_iam_policy.image_processor_sqs_policy.arn
  role       = aws_iam_role.image_processor.name
}

resource "aws_lambda_event_source_mapping" "image_processor_trigger" {
  function_name = aws_lambda_function.image_processor.arn
  event_source_arn = aws_sqs_queue.image_processing_queue.arn
  batch_size = 1
  scaling_config {
    maximum_concurrency = var.image_processor_max_concurrency
  }
  function_response_types = ["ReportBatchItemFailures"]
  depends_on = [aws_lambda_function.image_processor, aws_sqs_queue.image_processing_queue]
}

