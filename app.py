#!/usr/bin/python

import http.server
import logging
from prometheus_client import start_http_server

APP_PORT = 8081
METRICS_PORT = 8001

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

class HandleRequests(http.server.BaseHTTPRequestHandler):

    def do_GET(self):
        logging.info(f"Received request: {self.path} from {self.client_address}")
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes("<html><head><title>First Application</title></head><body style='color: #333; margin-top: 30px;'><center><h2>Welcome to our first Prometheus-Python application.</center></h2></body></html>", "utf-8"))

if __name__ == "__main__":
    logging.info(f"Starting metrics server on port {METRICS_PORT}")
    start_http_server(METRICS_PORT)

    logging.info(f"Starting HTTP server on port {APP_PORT}")
    server = http.server.HTTPServer(('0.0.0.0', APP_PORT), HandleRequests)
    
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        logging.info("Shutting down server")
        server.server_close()

