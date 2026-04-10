# agent.py

import os
from dotenv import load_dotenv
from typing import TypedDict, List
from pydantic import BaseModel, Field
from langgraph.graph import StateGraph, START, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage
from db import get_user
from memory import extract_and_merge_memory

load_dotenv()

# --- Global LLM instance ---
llm = ChatGoogleGenerativeAI(
    model="gemini-3-flash-preview",
    temperature=0,
)


# --- Structured output schema ---
class ReplyList(BaseModel):
    replies: list[str] = Field(description="Exactly 4 short, natural reply suggestions, each under 15 words")


# Structured LLM — guarantees ReplyList schema in response
structured_llm = llm.with_structured_output(ReplyList)


# --- State ---
class AgentState(TypedDict):
    user_id: str
    conversation_history: str
    user_context: str
    recommendations: List[str]


# ──────────────────────────────────────
#  Node 1: Fetch User Info / Context
# ──────────────────────────────────────
def fetch_user_context(state: AgentState) -> AgentState:
    """Pull user profile from Firestore and build a context string."""

    user = get_user(state["user_id"])

    if user is None:
        return {"user_context": "No user profile found. Use a neutral, friendly tone."}

    # Build context from basic info + learned memory
    parts = [f"User name: {user.get('name', 'Unknown')}"]

    memory = user.get("memory", {})
    for category, value in memory.items():
        if value:  # skip empty categories
            label = category.replace("_", " ").title()
            parts.append(f"{label}: {value}")

    context = "\n".join(parts)
    return {"user_context": context}


# ──────────────────────────────────────
#  Node 2: Get Recommendations
# ──────────────────────────────────────
def get_recommendations(state: AgentState) -> AgentState:
    """Use user profile + conversation to generate personalized reply suggestions."""

    result = structured_llm.invoke([
        SystemMessage(content=(
            "You are a reply suggestion engine for people who use assistive technology. "
            "You are given the user's profile and the current conversation. "
            "Generate exactly 4 short, natural replies that match the user's "
            "communication style and personality. "
            "Keep each reply under 15 words."
        )),
        HumanMessage(content=(
            f"USER PROFILE:\n{state['user_context']}\n\n"
            f"CONVERSATION:\n{state['conversation_history']}"
        )),
    ])

    return {"recommendations": result.replies}


# ──────────────────────────────────────
#  Node 3: Finalize Output
# ──────────────────────────────────────
def finalize_output(state: AgentState) -> AgentState:
    """Clean pass-through. Schema guarantees the list is already clean."""

    return {
        "recommendations": state["recommendations"],
        "user_context": "",
    }


# --- Build Graph ---
def build_agent():
    graph = StateGraph(AgentState)

    graph.add_node("fetch_user_context", fetch_user_context)
    graph.add_node("get_recommendations", get_recommendations)
    graph.add_node("finalize_output", finalize_output)

    # Linear flow: START -> context -> recommendations -> finalize -> END
    graph.add_edge(START, "fetch_user_context")
    graph.add_edge("fetch_user_context", "get_recommendations")
    graph.add_edge("get_recommendations", "finalize_output")
    graph.add_edge("finalize_output", END)

    return graph.compile()


# --- Run ---
if __name__ == "__main__":
    agent = build_agent()

    sample_conversation = """
    Friend: Hey, are you free this weekend?
    Friend: I was thinking we could grab lunch somewhere
    Friend: Maybe try that new Thai place downtown?
    """

    TEST_USER_ID = "test_user_001"

    # 1. Run the agent (uses profile from Firestore)
    result = agent.invoke({
        "user_id": TEST_USER_ID,
        "conversation_history": sample_conversation,
        "user_context": "",
        "recommendations": [],
    })

    print("Recommendations:", result["recommendations"])

    # 2. After conversation ends, update memory with new insights
    extract_and_merge_memory(TEST_USER_ID, sample_conversation)
