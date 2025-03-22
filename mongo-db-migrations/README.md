# MongoDb Migrations

Migrations that are run on the pipline.

## Adding a new migration

To add a new migration, use the command `npx migrate-mongo create <migration name>`. This will
create a migration template in the [migrations](./migrations) directory prepended with the current
date + time.