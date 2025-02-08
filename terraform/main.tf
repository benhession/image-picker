terraform {
  cloud {
    hostname     = "app.terraform.io"
    organization = "benhession-org"

    workspaces {
      tags = ["image-picker"]
    }
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.82.2"
    }
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 1.24.0"
    }
  }

  required_version = "~> 1.10.3"
}

provider "aws" {
  region = var.aws_region
}

provider "mongodbatlas" {
  public_key  = var.mongodb_atlas_public_key
  private_key = var.mongodb_atlas_private_key
}
