# MongoDb Migrations

Migrations that are run on the pipline.

## Adding a new migration

To add a new migration, use the command `npx migrate-mongo create <migration name>`. This will
create a migration template in the [migrations](./migrations) directory prepended with the current
date + time.

## Running the migration

The migration runs on the pipline.

To test locally the migration can be setup using the environment variables `MONGO_URL` and
`DATABASE_NAME`. Then run via `npx migrate-mongo up` and rolled back with `npx migrate-mongo down` 