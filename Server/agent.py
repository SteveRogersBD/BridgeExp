import os
import json
from typing import List, Optional, TypedDict

from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.graph import END, START, StateGraph
from google import genai

from models import AgentResponse, AgentState, ChatMessage, UserProfile

client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))


llm = ChatGoogleGenerativeAI(model="gemini-3-flash-preview",
                             api_key=os.getenv("GEMINI_API_KEY"))

def define_context(state:AgentState) -> AgentState:

    pass


def get_recommendation(state:AgentState) -> AgentState:
    pass

def get_response(state:AgentState) -> AgentState:
    pass



# agentic workflow logic and structure
workflow = StateGraph(AgentState)

workflow.add_node("define_context",define_context)
workflow.add_node("get_recommendation",get_recommendation)
workflow.add_node("get_response",get_response)

workflow.add_edge(START,"define_context")
workflow.add_edge("define_context","get_recommendation")
workflow.add_edge("define_context","get_response")
workflow.add_edge("get_response",END)

graph = workflow.compile()

print(graph)