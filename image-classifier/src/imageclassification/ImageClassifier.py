import os

import nltk
import torch
from PIL import Image
from nltk.corpus import wordnet as wn
from transformers import AutoModelForImageClassification, AutoImageProcessor

from imageclassification import MODEL_CHECKPOINT, NLTK_DATA_PATH


class ImageClassifier:

  def __init__(self, image: Image):
    """
    :param image: An instance of the Image object that contains the input image data to be processed.
    """
    self.image = image
    self.image_processor = AutoImageProcessor.from_pretrained(MODEL_CHECKPOINT,
                                                              use_fast=False)
    self.model = AutoModelForImageClassification.from_pretrained(
        MODEL_CHECKPOINT)
    if os.path.isdir(NLTK_DATA_PATH):
      nltk.data.path.append(NLTK_DATA_PATH)
      nltk.download('wordnet', download_dir=NLTK_DATA_PATH)

  def run(self):
    """
    Executes the process of image classification by leveraging a provided image, an image processing function,
    and a machine learning model. The method includes preprocessing the image, performing inference on the model,
    identifying the top 5 labels and scores, and mapping the labels to their respective categories.

    :return: A list of dictionaries. Each dictionary contains:
             - "label_id": Identifier for the predicted label.
             - "label": The label name corresponding to the prediction.
             - "score": Confidence score for the prediction.
             - "categories": A list of parent categories associated with the label.
    """
    with self.image as img:
      encoding = self.image_processor(img.convert('RGB'), return_tensors='pt')

    with torch.no_grad():
      outputs = self.model(**encoding)
      logits = outputs.logits
    top_k = torch.topk(logits, k=5)

    results = []
    for label_id, score in zip(
        top_k.indices[0], torch.softmax(top_k.values, dim=1)[0]):
      label = self.model.config.id2label[label_id.item()]

      results.append({
        "label_id": label_id.item(),
        "label": label,
        "score": score.item(),
        "categories": self.__find_parent_categories(label)
      })

    return results

  def __find_parent_categories(self, label):
    noun_synset = None
    hypernyms = []
    label = label.replace(' ', '_')

    # get the first noun as the most likely meaning, as they are ordered by usage
    synset_list = wn.synsets(label, pos=wn.NOUN)
    if len(synset_list) > 0:
      noun_synset = synset_list[0]

    def get_parent_categories_recursive(synset, hypernyms):
      for hypernym in synset.hypernyms():
        if hypernym.pos() == 'n' and hypernym.min_depth() > 5:
          get_parent_categories_recursive(hypernym, hypernyms)
          hypernyms.append(hypernym.lemmas()[0].name().replace('_', ' '))

    if noun_synset:
      get_parent_categories_recursive(noun_synset, hypernyms)

    return hypernyms
