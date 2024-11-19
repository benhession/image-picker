# image-picker

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Modules

### Microservice modules
- [image-picker-api](./image-picker-api/README.md) - the applications REST API
- [image-processor](./image-processor/README.md) - the image resizing service

### Shared modules
- [common](./common/README.md) - common classes
- [data](./data/README.md) - the persistence layer

### Deployment
- [Terraform](./terraform) - to deploy the services as AWS Lambda functions 
