resource "mongodbatlas_custom_db_role" "sandbox-lambda-access" {

  project_id = var.mongodb_atlas_project_id
  role_name  = "sandbox-lambda-access"

  dynamic "actions" {
    for_each = ["FIND", "INSERT", "REMOVE", "UPDATE", "CREATE_COLLECTION"]
    content {
      action = actions.value
      resources {
        collection_name = ""
        database_name   = var.mongodb_database_name
      }
    }
  }
}

resource "mongodbatlas_database_user" "sandbox_lambda_user" {

  auth_database_name = "$external"
  project_id         = var.mongodb_atlas_project_id
  username           = aws_iam_role.image_picker_lambda.arn
  aws_iam_type       = "ROLE"

  roles {
    role_name     = mongodbatlas_custom_db_role.sandbox-lambda-access.role_name
    database_name = "admin"
  }

  scopes {
    name = var.mongodb_atlas_cluster_name
    type = "CLUSTER"
  }
}