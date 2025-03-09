import streamlit as st
import requests
import json

# API Endpoint (Replace with your actual LLaMA API URL)
LLAMA_API_URL = "http://your-llama-server.com/generate"
SPRING_BOOT_API_URL = "http://your-backend-api.com/generate"

# Streamlit UI
st.title("Spring Boot AI Agent 🤖")
st.write("Hey Good morning! I am Spring Boot Agent. I can generate projects based on Orchestra. Let's get started!")

# Chat history
if "messages" not in st.session_state:
    st.session_state["messages"] = []

# JSON Configuration
if "config" not in st.session_state:
    st.session_state["config"] = {"input": {}, "output": {}}
    st.session_state["step"] = "input_type"

# Function to chat with LLaMA API
def chat_with_llama(prompt):
    response = requests.post(LLAMA_API_URL, json={"prompt": prompt, "max_tokens": 100})
    return response.json().get("text", "").strip()

# Function to move to the next step
def next_step():
    if st.session_state["step"] == "input_type":
        st.session_state["config"]["input"]["type"] = st.session_state["user_input"]
        st.session_state["step"] = "input_details"
    elif st.session_state["step"] == "input_details":
        st.session_state["config"]["input"]["serviceName"] = st.session_state["user_input"]
        st.session_state["step"] = "output_type"
    elif st.session_state["step"] == "output_type":
        st.session_state["config"]["output"]["type"] = st.session_state["user_input"]
        st.session_state["step"] = "output_details"
    elif st.session_state["step"] == "output_details":
        st.session_state["config"]["output"]["entityObject"] = st.session_state["user_input"]
        st.session_state["step"] = "confirm"

# Chatbot conversation
if st.session_state["step"] == "input_type":
    user_input = st.text_input("Choose Input Component (REST Consumer / Kafka Consumer):")
    if user_input:
        st.session_state["user_input"] = user_input
        next_step()
        st.rerun()

elif st.session_state["step"] == "input_details":
    user_input = st.text_input("Enter Service Name:")
    if user_input:
        st.session_state["user_input"] = user_input
        next_step()
        st.rerun()

elif st.session_state["step"] == "output_type":
    user_input = st.text_input("Choose Output Component (Kafka Producer / Oracle Client / Mongo Client / REST Producer):")
    if user_input:
        st.session_state["user_input"] = user_input
        next_step()
        st.rerun()

elif st.session_state["step"] == "output_details":
    user_input = st.text_input("Enter Entity Object:")
    if user_input:
        st.session_state["user_input"] = user_input
        next_step()
        st.rerun()

elif st.session_state["step"] == "confirm":
    st.json(st.session_state["config"])
    if st.button("Send API Request"):
        response = requests.post(SPRING_BOOT_API_URL, json=st.session_state["config"])
        st.write("API Response:", response.json())
