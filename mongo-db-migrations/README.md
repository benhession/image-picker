# MongoDb Migrations

Migrations are run on the pipline after a deployment. They can also be run during unit tests as
described below

## Adding a new migration

To add a new migration, use the command `npx migrate-mongo create <migration name>`. This will
create a migration template in the [migrations](./migrations) directory prepended with the current
date + time.

## Running the migration locally

To test locally, the database connection can be setup using the environment variables `MONGO_URL`
and `DATABASE_NAME`. Then the migration run via `npx migrate-mongo up` and rolled back with
`npx migrate-mongo down`.

## Testing

The migrations can be run during unit tests by adding a testImplementation dependency for`test-util`
and annotating the test class with `@QuarkusTestResource(MongoMigrationResource.class)`.
