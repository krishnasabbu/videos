import streamlit as st

# Page Config
st.set_page_config(page_title="ChatGPT Clone", page_icon="🤖", layout="wide")

# CSS for Custom Styling
st.markdown("""
    <style>
    /* Sidebar Background */
    [data-testid="stSidebar"] {
        background-color: #171717 !important;
        width: 300px !important;
    }
    
    [data-testid="ScrollToBottomContainer"] {
        width: 60% !important;
        margin-left: 8% !important;
    }

    /* Page Background */
    [data-testid="stAppViewContainer"] {
        background-color: #212121 !important;
    }

    /* Chat Styles */
    .user-msg { 
        background-color: #303030; color: white; padding: 10px 15px; 
        border-radius: 15px 15px 15px 15px; display: inline-block;
        max-width: 70%; margin-bottom: 30px;
    }
    .bot-msg { 
        max-width: 70%; margin-bottom: 30px;
    }
    .chat-container {
        height: 500px; overflow-y: auto;
    }
    .chat-box {
        display: flex; flex-direction: column;
        gap: 15px; padding: 10px;
    }
    .chat-right { align-self: flex-end; text-align: right; }
    .chat-left { align-self: flex-start; text-align: left; }

    /* Sidebar & Text Customization */
    [data-testid="stSidebar"] * {
        color: white !important;
    }

    /* Chat Input Height */
    .stChatInput textarea {
        min-height: 100px !important;
        max-height: 100px !important;
    }
    </style>
""", unsafe_allow_html=True)

# Sidebar
st.sidebar.title("ChatGPT Clone")
st.sidebar.button("New Chat")
st.sidebar.markdown("---")
st.sidebar.write("🔹 **History** (Coming soon)")
st.sidebar.write("🔹 **Settings** (Coming soon)")

# Initialize session state for chat history
if "messages" not in st.session_state:
    st.session_state.messages = []

# Display chat history
st.write("<div class='chat-container'><div class='chat-box'>", unsafe_allow_html=True)
for msg in st.session_state.messages:
    role, text = msg["role"], msg["text"]
    align_class = "chat-right" if role == "user" else "chat-left"
    msg_class = "user-msg" if role == "user" else "bot-msg"
    st.markdown(f"<div class='{align_class}'><div class='{msg_class}'>{text}</div></div>", unsafe_allow_html=True)
st.write("</div></div>", unsafe_allow_html=True)

# Chat input
user_input = st.chat_input("Ask me anything...")

# Process Input
if user_input:
    st.session_state.messages.append({"role": "user", "text": user_input})
    bot_response = "Sure! Let me help you with that..."  # Replace with real LLM call
    st.session_state.messages.append({"role": "bot", "text": bot_response})
    st.rerun()
