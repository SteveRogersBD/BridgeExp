# bridge_server.py - Simple Text-based Agent Relay

import asyncio
import json
import websockets
from agent import build_agent

# Configuration
PORT = 8765

# Pre-build your LangGraph agent
agent_graph = build_agent()

async def handle_connection(websocket):
    print("[Server] Android client connected.")
    
    async for message in websocket:
        try:
            data = json.loads(message)
            user_id = data.get("user_id", "test_user_001")
            user_text = data.get("text", "")

            if not user_text:
                continue

            print(f"[Server] Received text from {user_id}: {user_text}")

            # Run your existing LangGraph agent
            result = agent_graph.invoke({
                "user_id": user_id,
                "conversation_history": user_text, # Feed the transcript
                "user_context": "",
                "recommendations": [],
            })

            # Send the 4 recommendations back to Android
            response = {
                "type": "smart_replies",
                "replies": result["recommendations"]
            }
            await websocket.send(json.dumps(response))
            print(f"[Server] Sent {len(result['recommendations'])} recommendations.")

        except Exception as e:
            print(f"[Server] Error: {e}")

async def main():
    print(f"Starting Simple Bridge Relay on ws://localhost:{PORT}")
    async with websockets.serve(handle_connection, "0.0.0.0", PORT):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
