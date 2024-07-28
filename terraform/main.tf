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
  }

  required_version = "~> 1.9.1"
}

provider "aws" {
  region = var.aws_region
}
