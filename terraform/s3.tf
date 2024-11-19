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

resource "aws_s3_bucket_public_access_block" "image-picker-images" {
  bucket                  = aws_s3_bucket.image-picker-images.id
  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_ownership_controls" "image-picker-images" {
  bucket = aws_s3_bucket.image-picker-images.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_policy" "image-picker-images" {
  bucket = aws_s3_bucket.image-picker-images.bucket

  policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "${aws_s3_bucket.image-picker-images.arn}/*", # Allow access to all objects within the bucket
      },
    ],
  })

  depends_on = [aws_s3_bucket_public_access_block.image-picker-images]
}
