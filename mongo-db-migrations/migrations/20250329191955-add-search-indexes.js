module.exports = {
  /**
   * @param db {import('mongodb').Db}
   * @returns {Promise<void>}
   */
  async up(db) {
    const collection = await db.collection('images');
    const fileMetadataIndex = {
      name: "fileMetadataSearchIndex",
      definition: {
        "mappings": {
          "dynamic": false,
          "fields": {
            "filename": {
              "type": "string",
              "analyzer": "lucene.simple",
              "searchAnalyzer": "lucene.simple"
            },
            "tags": {
              "type": "string",
              "analyzer": "lucene.english",
              "searchAnalyzer": "lucene.english"
            },
            "aiTags": {
              "type": "string",
              "analyzer": "lucene.english",
              "searchAnalyzer": "lucene.english"
            },
            "status": {
              "type": "document",
              "fields": {
                "stage": {
                  "type": "string",
                  "analyzer": "lucene.keyword",
                  "searchAnalyzer": "lucene.keyword"
                }
              }
            }
          }
        },
        "fuzzy": {
          "maxEdits": 1,
          "prefixLength": 0,
          "maxExpansions": 10
        }
      }
    }
    await collection.createSearchIndex(fileMetadataIndex);
  },

  /**
   * @param db {import('mongodb').Db}
   * @returns {Promise<void>}
   */
  async down(db) {
    const collection = await db.collection('images');
    await collection.dropSearchIndex("fileMetadataSearchIndex");
  }
};
