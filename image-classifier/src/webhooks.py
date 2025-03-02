from flask import (
  Blueprint,
  jsonify
)

webhook = Blueprint("webhooks", __name__)


# TODO: remove this file when the lambda is tested

@webhook.route("/welcome", methods=["GET"])
def webhook_welcome():
  print('testing log from welcome function')
  return jsonify(status=200, message='Welcome to the webhook!')


@webhook.route("/", methods=["GET"])
def webhook_root():
  print('testing log from root function')
  return jsonify(status=200, message='OK')
