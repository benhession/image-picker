resource "aws_sqs_queue" "image_processing_queue" {
  name = "image_processing_queue"
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.image_processing_dlq.arn
    maxReceiveCount     = 2
  })
  visibility_timeout_seconds = 300
}

resource "aws_sqs_queue" "image_processing_dlq" {
  name = "image_processing_dlq"
}

resource "aws_sqs_queue_redrive_allow_policy" "image_processing_queue_redrive_allow_policy" {
  queue_url = aws_sqs_queue.image_processing_dlq.id
  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns = [aws_sqs_queue.image_processing_queue.arn]
  })
}

resource "aws_sqs_queue" "image_cropping_queue" {
  name = "image_cropping_queue"
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.image_cropping_dlq.arn
    maxReceiveCount     = 2
  })
  visibility_timeout_seconds = 300
}

resource "aws_sqs_queue" "image_cropping_dlq" {
  name = "image_cropping_dlq"
}

resource "aws_sqs_queue_redrive_allow_policy" "image_cropping_queue_redrive_allow_policy" {
  queue_url = aws_sqs_queue.image_cropping_dlq.id
  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue"
    sourceQueueArns = [aws_sqs_queue.image_cropping_queue.arn]
  })
}