locals {
  lambda_architecture     = terraform.workspace == "sandbox" ? "arm64" : "x86_64"
  mongodb_atlas_role_name = "${terraform.workspace}-${var.mongodb_atlas_base_role_name}"
}