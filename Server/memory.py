# memory.py — Post-conversation memory extraction and merge

from pydantic import BaseModel, Field
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage
from db import get_user, update_user_memory

# Reuse the same model as agent.py
llm = ChatGoogleGenerativeAI(
    model="gemini-3-flash-preview",
    temperature=0,
)


# --- Structured output schema ---
class UserMemory(BaseModel):
    communication_style: str = Field(description="How the user communicates — tone, formality, length preference. 1-3 sentences.")
    interests: str = Field(description="Topics, hobbies, and things the user enjoys. 1-3 sentences.")
    personality: str = Field(description="Personality traits observed from conversations. 1-3 sentences.")
    routines: str = Field(description="Schedule patterns, availability, and habits. 1-3 sentences.")
    social: str = Field(description="Social circle, relationships, and group dynamics. 1-3 sentences.")


# Structured LLM — guarantees UserMemory schema in response
structured_llm = llm.with_structured_output(UserMemory)


EXTRACT_AND_MERGE_PROMPT = """You are a user profiling system. Your job is to maintain a concise profile of a user based on their conversations.

CURRENT PROFILE:
{current_memory}

NEW CONVERSATION:
{conversation}

TASK:
- Analyze the new conversation for insights about the user.
- Merge any NEW information into the existing profile categories.
- Do NOT remove existing info unless it is directly contradicted.
- Do NOT duplicate information already present.
- Keep each category to 1-3 sentences max.
- If no new info is found for a category, keep it exactly as is.
- If a category is empty and no new info is found, leave it as an empty string.
"""


# ──────────────────────────────────────
#  Extract & merge memory from a conversation
# ──────────────────────────────────────
def extract_and_merge_memory(user_id: str, conversation: str) -> dict:
    """
    Run after a conversation ends.
    Reads the user's current memory, asks Gemini to merge new insights,
    and writes the updated memory back to Firestore.
    """

    # 1. Get current profile
    user = get_user(user_id)
    if user is None:
        print(f"[memory] User {user_id} not found. Skipping.")
        return {}

    current_memory = user.get("memory", {})

    # Format current memory for the prompt
    memory_str = "\n".join(
        f'  {key}: "{value}"' for key, value in current_memory.items()
    )

    # 2. Ask Gemini to merge — returns a typed UserMemory object
    result = structured_llm.invoke([
        SystemMessage(content=(
            "You are a user profiling system that maintains concise user profiles."
        )),
        HumanMessage(content=EXTRACT_AND_MERGE_PROMPT.format(
            current_memory=memory_str,
            conversation=conversation,
        )),
    ])

    # 3. Convert to dict and write back to Firestore
    updated_memory = result.model_dump()
    update_user_memory(user_id, updated_memory)

    print(f"[memory] Memory updated for user: {user_id}")
    return updated_memory


# ──────────────────────────────────────
#  Quick test
# ──────────────────────────────────────
if __name__ == "__main__":
    from dotenv import load_dotenv
    load_dotenv()

    test_conversation = """
    User: Yeah I'm free Saturday, let's do it!
    Friend: Awesome, the Thai place at 1pm?
    User: Perfect, I love pad thai. Can we invite Mike too?
    Friend: Sure! I'll text him.
    User: Cool, btw did you catch the basketball game last night?
    Friend: No I missed it, was it good?
    User: Insane game, went to overtime. We should go to one live sometime.
    """

    result = extract_and_merge_memory("test_user_001", test_conversation)
    print(f"\nUpdated memory:")
    for key, value in result.items():
        print(f"  {key}: {value}")
