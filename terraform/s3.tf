resource "aws_s3_bucket" "lambda_source_bucket" {
  bucket = var.lambda_source_bucket
}

resource "aws_s3_bucket_ownership_controls" "lambda-deployment" {
  bucket = aws_s3_bucket.lambda_source_bucket.id
  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

resource "aws_s3_bucket" "image-picker-images" {
  bucket = var.image_picker_bucket_name
}