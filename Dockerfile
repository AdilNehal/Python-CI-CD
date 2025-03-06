FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt \
    && apt-get update \
    && apt-get --no-cache-dir install -y curl net-tools
COPY . .
EXPOSE 8081 8001
CMD ["python", "app.py"]