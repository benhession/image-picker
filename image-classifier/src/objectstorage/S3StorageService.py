import io

import boto3
from PIL import Image

from objectstorage import S3_BUCKET_NAME
from objectstorage.AbstractObjectStorageService import \
  AbstractObjectStorageService
from objectstorage.ObjectNotFoundException import ObjectNotFoundException


class S3StorageService(AbstractObjectStorageService):
  def __init__(self):
    self.bucketName = S3_BUCKET_NAME
    self.client = boto3.client('s3')

  def get_image(self, object_key):
    """
    :param object_key: The key of the object to retrieve from the S3 bucket.
    :return: The image object opened from the downloaded S3 object data.
    :raises ObjectNotFoundException: If the specified object key does not exist in the bucket.
    """
    try:
      print("bucket:", S3_BUCKET_NAME)
      print("key:", object_key)

      response = self.client.get_object(Bucket=self.bucketName, Key=object_key)
      image_data = io.BytesIO(response['Body'].read())
      return Image.open(image_data)

    except self.client.exceptions.NoSuchKey as e:
      print(e)
      raise ObjectNotFoundException(
          f"Key not found while retrieving object from S3")
