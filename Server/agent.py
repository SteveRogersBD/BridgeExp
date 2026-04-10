# agent.py

import os
from dotenv import load_dotenv
from typing import TypedDict
from langgraph.graph import StateGraph, START, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage

load_dotenv()


# --- State ---
class AgentState(TypedDict):
    conversation_history: str
    context_summary: str
    recommendations: str


# --- Nodes ---
def analyze_context(state: AgentState) -> AgentState:
    """Step 1: Analyze the conversation history for tone, topic, and intent."""
    llm = ChatGoogleGenerativeAI(model="gemini-2.0-flash")

    response = llm.invoke([
        SystemMessage(content=(
            "You are a conversation analyst. Given a chat history, "
            "provide a brief summary of the conversation's topic, "
            "the emotional tone, and what the other person likely wants or needs. "
            "Keep it to 2-3 sentences."
        )),
        HumanMessage(content=state["conversation_history"]),
    ])

    return {"context_summary": response.content}


def generate_recommendations(state: AgentState) -> AgentState:
    """Step 2: Generate smart reply recommendations based on the context."""
    llm = ChatGoogleGenerativeAI(model="gemini-2.0-flash")

    response = llm.invoke([
        SystemMessage(content=(
            "You are a communication coach for people who use assistive technology. "
            "Based on the context summary of a conversation, generate exactly 3 short, "
            "natural reply suggestions the user could send next. "
            "Format them as a numbered list. Keep each reply under 15 words."
        )),
        HumanMessage(content=(
            f"Context summary:\n{state['context_summary']}\n\n"
            f"Original conversation:\n{state['conversation_history']}"
        )),
    ])

    return {"recommendations": response.content}


# --- Build Graph ---
def build_agent():
    graph = StateGraph(AgentState)

    # Add nodes
    graph.add_node("analyze_context", analyze_context)
    graph.add_node("generate_recommendations", generate_recommendations)

    # Linear flow: START -> analyze -> recommend -> END
    graph.add_edge(START, "analyze_context")
    graph.add_edge("analyze_context", "generate_recommendations")
    graph.add_edge("generate_recommendations", END)

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
        "context_summary": "",
        "recommendations": "",
    })

    print("=== Context Summary ===")
    print(result["context_summary"])
    print("\n=== Recommendations ===")
    print(result["recommendations"])