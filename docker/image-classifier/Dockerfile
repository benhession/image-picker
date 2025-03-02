FROM public.ecr.aws/lambda/python:3.12

ARG SYSTEM_NLTK_DATA_PATH=${SYSTEM_NLTK_DATA_PATH}
ENV MODEL_CHECKPOINT="google/vit-base-patch16-224"
ENV NLTK_DATA_PATH=/opt/nltk_data

# Copy wordnet data
COPY ${SYSTEM_NLTK_DATA_PATH} ${NLTK_DATA_PATH}

# Copy requirements.txt
COPY image-classifier/requirements.txt ${LAMBDA_TASK_ROOT}

# Install the specified packages
RUN pip install -r requirements.txt

# Copy function code
COPY image-classifier/src/ ${LAMBDA_TASK_ROOT}

CMD [ "main.handler" ]