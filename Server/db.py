# db.py — Firebase Firestore connection + user profile CRUD

import os
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timezone

# ──────────────────────────────────────
#  Initialize Firebase
# ──────────────────────────────────────

_KEY_PATH = os.path.join(os.path.dirname(__file__), "firebase_bridge_key.json")

cred = credentials.Certificate(_KEY_PATH)
firebase_admin.initialize_app(cred)

db = firestore.client()

# Collection reference
USERS_COLLECTION = "users"


# ──────────────────────────────────────
#  Default memory template
# ──────────────────────────────────────

DEFAULT_MEMORY = {
    "communication_style": "",
    "interests": "",
    "personality": "",
    "routines": "",
    "social": "",
}


# ──────────────────────────────────────
#  Create user (called at signup)
# ──────────────────────────────────────
def create_user(user_id: str, name: str) -> dict:
    """Create a new user profile with basic info and empty memory."""

    doc = {
        "user_id": user_id,
        "name": name,
        "memory": dict(DEFAULT_MEMORY),
        "created_at": datetime.now(timezone.utc).isoformat(),
        "updated_at": datetime.now(timezone.utc).isoformat(),
    }

    db.collection(USERS_COLLECTION).document(user_id).set(doc)
    print(f"[db] Created user: {user_id}")
    return doc


# ──────────────────────────────────────
#  Get user profile
# ──────────────────────────────────────
def get_user(user_id: str) -> dict | None:
    """Fetch a user profile from Firestore. Returns None if not found."""

    doc = db.collection(USERS_COLLECTION).document(user_id).get()
    if doc.exists:
        return doc.to_dict()
    return None


# ──────────────────────────────────────
#  Update user memory
# ──────────────────────────────────────
def update_user_memory(user_id: str, updated_memory: dict) -> None:
    """Overwrite the memory field for a user with the merged version."""

    db.collection(USERS_COLLECTION).document(user_id).update({
        "memory": updated_memory,
        "updated_at": datetime.now(timezone.utc).isoformat(),
    })
    print(f"[db] Updated memory for user: {user_id}")


# ──────────────────────────────────────
#  Quick test
# ──────────────────────────────────────
if __name__ == "__main__":
    # Test: create a user, read them back
    test_id = "test_user_001"

    create_user(test_id, name="Steve")

    user = get_user(test_id)
    print(f"\nFetched user: {user}")
