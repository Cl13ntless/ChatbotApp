FROM python:3.10.0-slim
RUN python -m pip install rasa[spacy]
RUN python -m spacy download de_core_news_lg
WORKDIR /app
ENV HOME=/app
COPY rasa .
RUN rasa train 
ENTRYPOINT ["rasa"]
CMD ["run", "--enable-api", "--port", "5005"]