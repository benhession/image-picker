from abc import abstractmethod


class AbstractObjectStorageService:
  @abstractmethod
  def get_image(self, object_key):
    pass
