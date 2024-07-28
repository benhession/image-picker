variable "lambda_source_bucket" {
  type    = string
  default = "image-picker-lambda-zips"
}

variable "image_picker_lambda_name" {
  type    = string
  default = "image-picker"
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
  type    = string
  default = "image-picker-images"
}

variable "mongodb_connection_string" {
  type = string
}

variable "mongodb_database_name" {
  type = string
}