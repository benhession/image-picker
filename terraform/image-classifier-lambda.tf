resource "aws_lambda_function" "image_classifier" {
  function_name = var.image_classifier_lambda_name
  role          = aws_iam_role.image_classifier_role.arn
  package_type  = "Image"
  image_uri     = var.image_classifier_image_url
  architectures = [local.lambda_architecture]
  timeout       = 300
  memory_size   = 2048

  ephemeral_storage {
    size = 1024
  }

  environment {
    variables = tomap({
      S3_BUCKET_NAME = var.image_picker_bucket_name
    })
  }
}

resource "aws_cloudwatch_log_group" "image_classifier_log_group" {
  name              = "/aws/lambda/${aws_lambda_function.image_classifier.function_name}"
  retention_in_days = 30
}

resource "aws_iam_role" "image_classifier_role" {
  name               = "${var.image_processor_lambda_name}-lambda"
  assume_role_policy = data.aws_iam_policy_document.image_classifier_assume_lambda_policy.json
}

data "aws_iam_policy_document" "image_classifier_assume_lambda_policy" {
  statement {
    effect = "Allow"

    principals {
      type = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role_policy_attachment" "image_classifier_s3_policy_attachment" {
  policy_arn = aws_iam_policy.image_classifier_s3_policy
  role       = aws_iam_role.image_classifier_role.name
}

resource "aws_iam_policy" "image_classifier_s3_policy" {
  name   = "image classifier_s3_policy"
  policy = data.aws_iam_policy_document.image_classifier_s3_policy.json
}

data "aws_iam_policy_document" "image_classifier_s3_policy" {
  statement {
    actions = [
      "s3:GetObject",
      "s3:ListObjects",
      "s3:ListBucket"
    ]
    resources = [
      aws_s3_bucket.image-picker-images.arn,
      "${aws_s3_bucket.image-picker-images.arn}/*"
    ]
  }
}