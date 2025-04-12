FROM python:3.11-slim-bookworm

WORKDIR /app

# Update system packages first
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y --no-install-recommends curl net-tools && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip setuptools wheel && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8081 8001
CMD ["python", "app.py"]