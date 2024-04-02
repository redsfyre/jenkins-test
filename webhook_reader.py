from flask import Flask, request
import urllib.parse
import json

app = Flask(__name__)

@app.route("/jenkins", methods=["POST"])
def webhook():
    try:
        # Access headers directly using request.headers
        headers = dict(request.headers)  # Make a copy for potential modification
        print("Headers:", headers)

        # Check for JSON payload or use form data as fallback
        if request.is_json:
            data = request.get_json()
            print("JSON payload:", json.dumps(data, indent=4))
        elif request.content_type == "application/json":
            form = request.form
            encoded_payload = form['payload']
            payload = urllib.parse.unquote_plus(encoded_payload)
            print("Webhook request received (form data)!")
            print("Payload:", payload)
        else:
            print("Unsupported content type")
            return "Unsupported content type", 415

        # Process data and headers as needed
        # ...

        return "Success", 200

    except Exception as e:
        print(f"Error: {e}")
        return "Internal server error", 500

if __name__ == "__main__":
    app.run(debug=True)
