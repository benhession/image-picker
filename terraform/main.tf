terraform {
  cloud {
    hostname = "app.terraform.io"
    organization = "benhession-org"

    workspaces {
      name = "sandbox"
    }
  }

  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 5.57.0"
    }
    mongodbatlas = {
      source = "mongodb/mongodbatlas"
      version = "~> 1.18.1"
    }
  }

  required_version = "~> 1.9.1"
}

provider "aws" {
  region = var.aws_region
}

provider "mongodbatlas" {
  public_key = var.mongodb_atlas_public_key
  private_key = var.mongodb_atlas_private_key
}
