data "aws_ecr_repository" "image_classifier_repository" {
  name = var.image_classifier_ecr_name
}

resource "aws_ecr_lifecycle_policy" "image_classifier_repo_lifecycle_policy" {
  policy     = data.aws_ecr_lifecycle_policy_document.image_classifier_repo_lifecycle_policy.json
  repository = data.aws_ecr_repository.image_classifier_repository.name
}

data "aws_ecr_lifecycle_policy_document" "image_classifier_repo_lifecycle_policy" {
  rule {
    priority    = 1
    description = "Keep 2 most recent images"
    selection {
      count_number = 2
      count_type   = "imageCountMoreThan"
      tag_status   = "any"
    }
    action {
      type = "expire"
    }
  }
}