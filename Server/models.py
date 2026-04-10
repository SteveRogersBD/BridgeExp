from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class UserProfile:
    user_id: str
    name: str
    communication_style: str


@dataclass
class ChatMessage:
    sender: str
    text: str
    timestamp: str
    message_type: str = "text"


@dataclass
class RecommendedChat:
    assistant_message: str = ""
    suggested_replies: List[str] = field(default_factory=list)


@dataclass
class AgentState:
    user_profile: UserProfile
    conversation_history: List[ChatMessage] = field(default_factory=list)
    latest_user_message: str = ""
    recommendation: Optional[RecommendedChat] = None


class AgentResponse:
    pass