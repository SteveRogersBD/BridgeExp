# agent.py

import os
from dotenv import load_dotenv
from typing import TypedDict, List
from langgraph.graph import StateGraph, START, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage

load_dotenv()

# --- Global LLM instance ---
llm = ChatGoogleGenerativeAI(
    model="gemini-3-flash-preview",
    temperature=0,
    max_output_tokens=512,
)


# --- State ---
class AgentState(TypedDict):
    conversation_history: str
    user_context: str
    raw_recommendations: str
    recommendations: List[str]


# ──────────────────────────────────────
#  Node 1: Fetch User Info / Context
# ──────────────────────────────────────
def fetch_user_context(state: AgentState) -> AgentState:
    """Analyze the conversation to extract topic, tone, and intent."""

    response = llm.invoke([
        SystemMessage(content=(
            "You are a conversation analyst. "
            "Given a chat history, provide a brief summary covering:\n"
            "- The main topic\n"
            "- The emotional tone\n"
            "- What the other person likely expects as a response\n\n"
            "Keep it to 2-3 sentences max. No bullet points, just plain text."
        )),
        HumanMessage(content=state["conversation_history"]),
    ])

    return {"user_context": str(response.content)}


# ──────────────────────────────────────
#  Node 2: Get Recommendations
# ──────────────────────────────────────
def get_recommendations(state: AgentState) -> AgentState:
    """Use the extracted context to generate reply suggestions."""

    response = llm.invoke([
        SystemMessage(content=(
            "You are a reply suggestion engine for people who use assistive technology. "
            "Given the conversation context below, suggest exactly 4 short, natural replies "
            "the user could send next. Keep each reply under 15 words.\n\n"
            "Return ONLY the replies, one per line, numbered 1-4. Nothing else."
        )),
        HumanMessage(content=(
            f"Context: {state['user_context']}\n\n"
            f"Conversation:\n{state['conversation_history']}"
        )),
    ])

    return {"raw_recommendations": str(response.content)}


# ──────────────────────────────────────
#  Node 3: Finalize Output
# ──────────────────────────────────────
def finalize_output(state: AgentState) -> AgentState:
    """Parse the raw LLM output into a clean list of strings."""

    raw = state["raw_recommendations"]
    if isinstance(raw, list):
        raw = "\n".join(str(item) for item in raw)
    lines = raw.strip().splitlines()
    cleaned = []
    for line in lines:
        text = line.strip()
        # Strip leading numbers like "1. " or "1) "
        if text and len(text) > 2 and text[0].isdigit() and text[1] in ".)" :
            text = text[2:].strip()
        if text:
            cleaned.append(text)

    return {
        "recommendations": cleaned,
        "user_context": "",
        "raw_recommendations": "",
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

    result = agent.invoke({
        "conversation_history": sample_conversation,
        "user_context": "",
        "raw_recommendations": "",
        "recommendations": [],
    })

    # Clean output: just the list
    print(result["recommendations"])