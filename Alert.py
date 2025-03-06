import os
import faiss
import pickle
import logging
from langchain.document_loaders import DirectoryLoader, UnstructuredFileLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.embeddings import HuggingFaceEmbeddings  # Open-source alternative to OpenAI
from langchain.vectorstores import FAISS
from langchain.chains import RetrievalQA
from langchain.llms import CTransformers  # Using Llama 2 or GPT4All

# Configure logging
logging.basicConfig(level=logging.INFO)


# Step 1: Load Documents
def load_documents(directory):
    """
    Load documents from a directory. Supports multiple file formats.
    """
    try:
        loader = DirectoryLoader(directory, glob="*.*", loader_cls=UnstructuredFileLoader)
        documents = loader.load()
        logging.info(f"Loaded {len(documents)} documents from {directory}")
        return documents
    except Exception as e:
        logging.error(f"Error loading documents: {e}")
        raise


# Step 2: Split text for better embeddings
def split_text(documents):
    """
    Split text into chunks with semantic awareness.
    """
    try:
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=1000,
            chunk_overlap=200,
            separators=["\n\n", "\n", ".", "!", "?", " ", ""]
        )
        chunks = text_splitter.split_documents(documents)
        logging.info(f"Split documents into {len(chunks)} chunks")
        return chunks
    except Exception as e:
        logging.error(f"Error splitting text: {e}")
        raise


# Step 3: Convert text into embeddings and store in FAISS
def create_vector_store(chunks, faiss_index_path="faiss_index"):
    """
    Create a FAISS vector store using open-source embeddings.
    """
    try:
        embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
        vector_store = FAISS.from_documents(chunks, embeddings)

        # Save the FAISS index and metadata
        faiss.write_index(vector_store.index, faiss_index_path)
        with open("faiss_store.pkl", "wb") as f:
            pickle.dump(vector_store, f)
        logging.info("Vector store created and saved successfully")
        return vector_store
    except Exception as e:
        logging.error(f"Error creating vector store: {e}")
        raise


# Step 4: Load FAISS index
def load_vector_store(faiss_index_path="faiss_index"):
    """
    Load the FAISS vector store from disk.
    """
    try:
        embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
        index = faiss.read_index(faiss_index_path)
        with open("faiss_store.pkl", "rb") as f:
            vector_store = pickle.load(f)
        vector_store.index = index
        logging.info("Vector store loaded successfully")
        return vector_store
    except Exception as e:
        logging.error(f"Error loading vector store: {e}")
        raise


# Step 5: Query the knowledge base
def query_bot(query, vector_store):
    """
    Query the knowledge base and generate a response using Llama 2/GPT4All.
    """
    try:
        llm = CTransformers(model="llama-2-7b-chat.ggmlv3.q4_0.bin", model_type="llama")
        retriever = vector_store.as_retriever(search_kwargs={"k": 5})
        qa_chain = RetrievalQA.from_chain_type(llm=llm, chain_type="stuff", retriever=retriever)
        response = qa_chain.run(query)
        logging.info(f"Generated response: {response}")
        return response
    except Exception as e:
        logging.error(f"Error querying bot: {e}")
        return "Sorry, I encountered an issue while processing your request."


# Main Execution
if __name__ == "__main__":
    try:
        # Load and process documents
        documents = load_documents("./alerts_docs")  # Folder with alert docs
        processed_chunks = split_text(documents)
        vector_store = create_vector_store(processed_chunks)

        # Interactive query loop
        while True:
            user_query = input("Ask about an alert (type 'exit' to quit): ").strip()
            if user_query.lower() in ["exit", "quit"]:
                break

            # Query the bot and display the response
            answer = query_bot(user_query, vector_store)
            print(f"Bot: {answer}")

    except KeyboardInterrupt:
        logging.info("Program terminated by user.")
    except Exception as e:
        logging.error(f"Unexpected error: {e}")
