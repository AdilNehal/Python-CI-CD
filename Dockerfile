FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN apt-get update && apt-get install -y curl net-tools \
    && pip install --no-cache-dir -r requirements.txt
COPY . .
EXPOSE 8081 8001
CMD ["python", "app.py"]