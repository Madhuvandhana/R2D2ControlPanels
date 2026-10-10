#!/usr/bin/env python3
import subprocess
from flask import Flask, jsonify

app = Flask(__name__)

# Route to trigger startup script/command
@app.route('/start-r2-software', methods=['GET'])
def start_software():
    try:
        # Replace this command with the actual path to your Jetson startup shell script or python script
        # Example: subprocess.Popen(["/bin/bash", "/home/jetson/start_r2.sh"])
        # For testing, we run a simple echo command:
        subprocess.Popen(["echo", "R2-D2 Software Startup Triggered"])
        
        return jsonify({
            "status": "success",
            "message": "R2-D2 software startup command successfully triggered."
        }), 200
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

if __name__ == '__main__':
    # Listens on all network interfaces on port 5000
    print("R2-D2 Jetson Listener API active. Listening on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000)
