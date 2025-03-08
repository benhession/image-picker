from flask import Blueprint, request, jsonify

from imageclassification.ImageClassifier import ImageClassifier
from objectstorage.AbstractObjectStorageService import \
  AbstractObjectStorageService
from objectstorage.ObjectNotFoundException import ObjectNotFoundException
from objectstorage.S3StorageService import S3StorageService

image_classifier_controller = Blueprint("image-classifier-controller", __name__)


@image_classifier_controller.route("/", methods=["POST"])
def classify():
  # Verify body
  data = request.get_json()
  if not data or data.get("objectKey") is None or len(
      data.get("objectKey")) == 0:
    return jsonify({
      "error": "No request data, expected {\"objectKey\": \"example/key\"}}"}), 400

  object_key = data.get("objectKey")

  # get image from s3
  try:
    object_storage_service: AbstractObjectStorageService = S3StorageService()
    image = object_storage_service.get_image(object_key)
  except ObjectNotFoundException:
    return jsonify({"error": f"key {object_key} not found"}), 404

  # get classification
  image_classifier = ImageClassifier(image)
  results = image_classifier.run()

  # return results
  return jsonify(results), 200
