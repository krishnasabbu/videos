import os
import faiss
import pickle
import logging
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from ctransformers import AutoModelForCausalLM

# Configure logging
logging.basicConfig(level=logging.INFO)

class AlertQueryBot:
    def __init__(self, directory="./alerts_docs", faiss_index_path="faiss_index", metadata_path="faiss_meta13.pkl", top_k=1):
        self.directory = directory
        self.faiss_index_path = faiss_index_path
        self.metadata_path = metadata_path
        self.top_k = top_k

        self.index = None
        self.chunks = []
        self.vectorizer = None

        # Load existing FAISS index or create a new one
        if os.path.exists(self.faiss_index_path) and os.path.exists(self.metadata_path):
            self.load_vector_store()
        else:
            self.create_vector_store()

    # Step 1: Load and Split Documents into Chunks
    def load_documents(self):
        """Loads text files from a directory."""
        documents = []
        filenames = []

        try:
            for filename in os.listdir(self.directory):
                if filename.endswith(".txt"):
                    file_path = os.path.join(self.directory, filename)
                    with open(file_path, "r", encoding="utf-8") as file:
                        documents.append(file.read())
                        filenames.append(filename)

            logging.info(f"Loaded {len(documents)} documents.")
            return documents, filenames
        except Exception as e:
            logging.error(f"Error loading documents: {e}")
            raise

    def split_text(self, documents, filenames, chunk_size=300, overlap=50):
        """Splits documents into overlapping chunks."""
        chunks = []
        metadata = []

        try:
            for doc, filename in zip(documents, filenames):
                words = doc.split()
                for i in range(0, len(words), chunk_size - overlap):
                    chunk_text = " ".join(words[i:i + chunk_size])
                    chunks.append(chunk_text)
                    metadata.append({"filename": filename, "start_idx": i})  # Track source info

            logging.info(f"Split documents into {len(chunks)} chunks.")
            return chunks, metadata
        except Exception as e:
            logging.error(f"Error splitting text: {e}")
            raise

    # Step 2: Create FAISS Vector Store
    def create_vector_store(self):
        """Creates a FAISS vector store using TF-IDF embeddings."""
        try:
            documents, filenames = self.load_documents()
            self.chunks, self.metadata = self.split_text(documents, filenames)

            self.vectorizer = TfidfVectorizer()
            tfidf_matrix = self.vectorizer.fit_transform(self.chunks).toarray()

            dimension = tfidf_matrix.shape[1]
            self.index = faiss.IndexFlatL2(dimension)
            self.index.add(tfidf_matrix.astype(np.float32))

            # Save FAISS index and metadata
            faiss.write_index(self.index, self.faiss_index_path)
            with open(self.metadata_path, "wb") as f:
                pickle.dump((self.chunks, self.metadata, self.vectorizer), f)

            logging.info(f"Vector store created with {len(self.chunks)} chunks.")
        except Exception as e:
            logging.error(f"Error creating vector store: {e}")
            raise

    # Step 3: Load FAISS Index
    def load_vector_store(self):
        """Loads the FAISS index and TF-IDF metadata correctly."""
        try:
            self.index = faiss.read_index(self.faiss_index_path)
            with open(self.metadata_path, "rb") as f:
                self.chunks, self.metadata, self.vectorizer = pickle.load(f)

            logging.info("Vector store loaded successfully.")
        except Exception as e:
            logging.error(f"Error loading vector store: {e}")
            raise

    # Step 4: Retrieve Relevant Text (Fixed!)
    def retrieve_relevant_text(self, query):
        """Retrieves only the most relevant chunks using FAISS."""
        try:
            query_vector = self.vectorizer.transform([query]).toarray().astype(np.float32)
            distances, indices = self.index.search(query_vector, self.top_k)

            results = []
            for idx in indices[0]:
                if idx < len(self.chunks):
                    chunk_info = self.metadata[idx]
                    results.append(f"📜 {chunk_info['filename']} (Chunk {chunk_info['start_idx']}):\n{self.chunks[idx]}")

            return "\n\n".join(results) if results else "No relevant information found."
        except Exception as e:
            logging.error(f"Error retrieving relevant text: {e}")
            return "No relevant information found."

    # Step 5: Query the LLM (Llama 2/GPT4All)
    def query_bot(self, query, context):
        """Generates a response using a local Llama 2 model."""
        try:
            llm = AutoModelForCausalLM.from_pretrained(
                "llama-2-7b-chat.ggmlv3.q4_0.bin",  # Set your local model path
                model_type="llama"
            )
            prompt = f"User Query: {query}\nContext: {context}\nAnswer:"
            response = llm(prompt)

            return response.strip()
        except Exception as e:
            logging.error(f"Error generating response: {e}")
            return "Sorry, I encountered an issue while processing your request."

    # Step 6: Interactive Chat
    def interactive_chat(self):
        """Runs an interactive chat loop."""
        try:
            while True:
                user_query = input("\n🔍 Ask about an alert (type 'exit' to quit): ").strip()
                if user_query.lower() in ["exit", "quit"]:
                    break

                relevant_text = self.retrieve_relevant_text(user_query)
                print(f"\n📌 **Relevant Context:**\n{relevant_text}")

                answer = self.query_bot(user_query, relevant_text) if relevant_text else "No relevant information found."
                print(f"\n🤖 **Bot:** {answer}\n")

        except KeyboardInterrupt:
            logging.info("Program terminated by user.")
        except Exception as e:
            logging.error(f"Unexpected error: {e}")

# Main Execution
if __name__ == "__main__":
    bot = AlertQueryBot()
    bot.interactive_chat()
