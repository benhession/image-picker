variable "lambda_source_bucket_name" {
  type = string
}

variable "image_picker_api_lambda_name" {
  type    = string
  default = "image-picker-api"
}

variable "image_processor_lambda_name" {
  type    = string
  default = "image-processor"
}

variable "image_cropper_lambda_name" {
  type    = string
  default = "image-cropper"
}

variable "image_classifier_lambda_name" {
  type    = string
  default = "image-classifier"
}

variable "image_classifier_image_url" {
  type = string
}

variable "image_classifier_ecr_name" {
  type    = string
  default = "image-picker/image-classifier"
}

variable "auth_server_url" {
  type = string
}

variable "oidc_client_id" {
  type = string
}

variable "oidc_client_secret" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "image_picker_bucket_name" {
  type = string
}

variable "mongodb_connection_string" {
  type = string
}

variable "mongodb_database_name" {
  type = string
}

variable "mongodb_atlas_public_key" {
  type = string
}

variable "mongodb_atlas_private_key" {
  type = string
}

variable "mongodb_atlas_project_id" {
  type = string
}

variable "mongodb_atlas_base_role_name" {
  type    = string
  default = "lambda-access"
}

variable "mongodb_atlas_cluster_name" {
  type    = string
  default = "blog-cluster"
}

variable "image_processor_max_concurrency" {
  type    = number
  default = 10
}

variable "image_cropper_max_concurrency" {
  type    = number
  default = 10
}