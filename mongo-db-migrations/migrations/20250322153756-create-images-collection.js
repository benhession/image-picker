module.exports = {
  /**
   * @param db {import('mongodb').Db}
   * @returns {Promise<void>}
   */
  async up(db) {
    const collection = await db.createCollection('images');
    await collection.createIndex({parentKey: 1}, {unique: true})
  },

  /**
   * @param db {import('mongodb').Db}
   * @returns {Promise<void>}
   */
  async down(db) {
    await db.collection('images').drop();
  }
};
