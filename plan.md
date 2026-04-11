# 🌉 Bridge Implementation Plan: Python Backend + Gemini Live

This plan outlines the architecture for using a **Python Backend Server** alongside the Android app. By using Python, we avoid the extreme dependency conflicts caused by mixing Google Cloud backend SDKs with the Firebase Android SDK, and we gain access to the far more robust Python AI ecosystem for GenAI and LangGraph.

## Phase 1: Python Backend Foundation (Steps 1-2)

### Step 1: Server Setup & Dependencies
- Set up the Python `/Server` directory with `google-genai` and `firebase-admin` via `requirements.txt` or `Pipfile`.
- Initialize `firebase_admin` using the service account key to allow the backend to safely access Firestore and Auth without conflicting with Android.

### Step 2: The Agent Brain
- Create a central Python module (e.g. `agent_manager.py`).
- Implement the baseline Gemini Multimodal Live API logic (WebSockets) in Python.
- Define a base System Prompt instructing the agent to act as a "proactive observer", saving user traits/routines into Firestore.

---

## Phase 2: Live Integration (Steps 3-4)

### Step 3: Android <-> Python Communication 
- Update `ChatActivity.java` in the Android app to connect to the Python backend via WebSockets rather than processing the Gemini Live API directly.
- Ensure audio byte streams from the microphone are passed securely to the Python server.

### Step 4: Proactive Intelligence & Tools
- Define Python-based Function Calling (Tools) for:
    - `updateUserProfile(key, value)`: Automatically triggered when the model learns something about the user, pushing to Firestore.
    - `getSmartReplies()`: Generating context-aware tap-to-reply suggestions.
    - `executeTask(intent)`: For the agentic execution feature (booking rides, setting reminders).

---

## Phase 3: Polish (Step 5)

### Step 5: Personalized Voice (ElevenLabs)
- Centralize ElevenLabs generation. We can generate the TTS audio directly on the Python backend and stream it back to Android, minimizing the device's CPU load and shrinking the Android app size.

---

## Implementation Status
- [ ] Step 1: Server Setup & Dependencies
- [ ] Step 2: The Agent Brain
- [ ] Step 3: Android <-> Python Communication
- [ ] Step 4: Proactive Intelligence
- [ ] Step 5: ElevenLabs Integration

