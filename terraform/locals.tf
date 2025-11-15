locals {
  lambda_architecture       = terraform.workspace == "sandbox" ? "arm64" : "x86_64"
  mongodb_atlas_role_name   = "${terraform.workspace}-${var.mongodb_atlas_base_role_name}"
  lambda_source_bucket_name = "${var.bucket_prefix}-ben-hession-image-picker-zips-${terraform.workspace}"
  image_picker_bucket_name  = "${var.bucket_prefix}-image-picker-images-${terraform.workspace}"
}
