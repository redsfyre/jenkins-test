#!/bin/bash

# Port to listen on (change if needed)
PORT=5000

# Function to parse request headers
parse_headers() {
  local headers="$1"
  while IFS=':' read -r name value; do
    echo -e "Header: $name -> $value"
  done <<< "$headers"
}

# Start listening for connections
nc -kl "$PORT" | while IFS= read -r request; do
  # Split request line
  read -r method url protocol <<< "$request"

  # Extract headers
  headers=$(sed '/^$/d' <<< "$request")

  # Print request details
  echo "Request Method: $method"
  echo "Request URL: $url"
  echo "Request Protocol: $protocol"

  # Parse and display headers
  parse_headers "$headers"

  # Check if there's a body (POST request usually)
  if [[ $method == "POST" ]]; then
    # Read remaining data as the request body
    body=$(cat -)
    echo "Request Body:"
    echo "$body"
  fi

  # Send a basic response (replace with your logic)
  echo -e "HTTP/1.1 200 OK\nContent-Type: text/plain\n\nHello, World!"
done

echo "Server stopped"

