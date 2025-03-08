from awsgi import response
from flask import Flask

from ImageClassifierController import image_classifier_controller


def create_app():
  app = Flask(__name__)

  # Import and register blueprints
  app.register_blueprint(image_classifier_controller)

  return app


def handler(event, context):
  print("Flask app started")
  app = create_app()

  return response(app, event, context)
