resource "mongodbatlas_custom_db_role" "mongo_db_lambda_access" {

  project_id = var.mongodb_atlas_project_id
  role_name  = var.mongodb_atlas_role_name

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

resource "mongodbatlas_database_user" "image_picker_api" {

  auth_database_name = "$external"
  project_id         = var.mongodb_atlas_project_id
  username           = aws_iam_role.image_picker_lambda_api.arn
  aws_iam_type       = "ROLE"

  roles {
    role_name     = mongodbatlas_custom_db_role.mongo_db_lambda_access.role_name
    database_name = "admin"
  }

  scopes {
    name = var.mongodb_atlas_cluster_name
    type = "CLUSTER"
  }
}

resource "mongodbatlas_database_user" "image_processor" {
  auth_database_name = "$external"
  project_id         = var.mongodb_atlas_project_id
  username           = aws_iam_role.image_processor.arn
  aws_iam_type       = "ROLE"

  roles {
    database_name = "admin"
    role_name     = mongodbatlas_custom_db_role.mongo_db_lambda_access.role_name
  }

  scopes {
    name = var.mongodb_atlas_cluster_name
    type = "CLUSTER"
  }
}