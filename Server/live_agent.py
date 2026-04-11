# live_agent.py - Gemini Multimodal Live API Implementation

import os
import asyncio
import base64
from dotenv import load_dotenv
from google import genai
from db import get_user

load_dotenv()

# --- Configuration ---
MODEL_ID = "gemini-2.0-flash-exp"  # The model supporting Multimodal Live API
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")

if not GOOGLE_API_KEY:
    raise ValueError("GOOGLE_API_KEY not found in environment.")

client = genai.Client(api_key=GOOGLE_API_KEY, http_options={'api_version': 'v1alpha'})

# ──────────────────────────────────────
#  Build System Instruction from Memory
# ──────────────────────────────────────
def get_system_instruction(user_id: str) -> str:
    """Creates a personalized prompt based on user memory."""
    user = get_user(user_id)
    
    base_prompt = (
        "You are 'Bridge', a proactive and empathetic AI assistant. "
        "Your goal is to support the user by observing their conversation and environment (via audio/video) "
        "and providing helpful, concise assistance. "
    )
    
    if user:
        memory = user.get("memory", {})
        context = f"\nUSER INFO:\nName: {user.get('name', 'User')}\n"
        for key, val in memory.items():
            if val:
                context += f"{key.replace('_', ' ').title()}: {val}\n"
        
        base_prompt += context
        base_prompt += "\nUse this information to match the user's communication style and be more helpful."
    
    base_prompt += "\nKeep responses short and conversational, as they will be spoken aloud."
    return base_prompt

# ──────────────────────────────────────
#  Live Session Handler
# ──────────────────────────────────────
async def run_live_agent(user_id: str):
    """
    Simulates a live session. In a real scenario, this would receive 
    audio bytes from a WebSocket (from Android) and forward them.
    """
    system_instruction = get_system_instruction(user_id)
    
    # 1. Connect to the Live API
    config = {
        "system_instruction": system_instruction,
        "generation_config": {
            "speech_config": {
                "voice_config": {
                    "prebuilt_voice_config": {
                        "voice_name": "Puck"  # Options: Puck, Charon, Kore, Fenrir, Aoede
                    }
                }
            }
        }
    }

    async with client.models.live.connect(model=MODEL_ID, config=config) as session:
        print(f"[LiveAgent] Connected to Gemini for user: {user_id}")
        
        # This loop would handle bidirectional communication
        # In a real implementation, we would have tasks to:
        # 1. Read audio chunks from Android and send via `session.send()`
        # 2. Read messages from `session.receive()` and send audio back to Android
        
        # Simple test message (Text-to-Speech)
        print("[LiveAgent] Sending initial greeting...")
        await session.send(input="Hello! I am Bridge. How can I help you today?", end_of_turn=True)
        
        async for message in session.receive():
            if message.server_content:
                # Handle text/audio parts
                if message.server_content.model_turn:
                    for part in message.server_content.model_turn.parts:
                        if part.text:
                            print(f"Gemini: {part.text}")
                        elif part.inline_data:
                            # This is the raw audio bytes from the default TTS
                            # We would send this to the Android app for playback
                            print(f"[Audio Chunk Received: {len(part.inline_data.data)} bytes]")
            
            if message.tool_call:
                # Placeholder for function calling (Step 4 of of your plan)
                print(f"[Tool Call]: {message.tool_call}")

if __name__ == "__main__":
    # Test with a dummy user
    asyncio.run(run_live_agent("test_user_001"))
