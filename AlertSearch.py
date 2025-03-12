import os
import faiss
import pickle
import logging
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import normalize

# Configure logging
logging.basicConfig(level=logging.INFO)

class AlertQueryBot:
    def __init__(self, directory="./alerts_docs", faiss_index_path="faiss_index345", metadata_path="faiss_meta345c.pkl", similarity_threshold=0.2):
        self.directory = directory
        self.faiss_index_path = faiss_index_path
        self.metadata_path = metadata_path
        self.similarity_threshold = similarity_threshold

        self.index = None
        self.chunks = []
        self.vectorizer = None

        # Load existing FAISS index or create a new one
        if os.path.exists(self.faiss_index_path) and os.path.exists(self.metadata_path):
            self.load_vector_store()
        else:
            self.create_vector_store()

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
                    metadata.append({"filename": filename, "start_idx": i})

            logging.info(f"Split documents into {len(chunks)} chunks.")
            return chunks, metadata
        except Exception as e:
            logging.error(f"Error splitting text: {e}")
            raise

    def create_vector_store(self):
        """Creates a FAISS vector store using TF-IDF embeddings."""
        try:
            documents, filenames = self.load_documents()
            self.chunks, self.metadata = self.split_text(documents, filenames)

            self.vectorizer = TfidfVectorizer()
            tfidf_matrix = normalize(self.vectorizer.fit_transform(self.chunks).toarray(), norm='l2')

            dimension = tfidf_matrix.shape[1]
            self.index = faiss.IndexFlatL2(dimension)
            self.index.add(tfidf_matrix.astype(np.float32))

            faiss.write_index(self.index, self.faiss_index_path)
            with open(self.metadata_path, "wb") as f:
                pickle.dump((self.chunks, self.metadata, self.vectorizer), f)

            logging.info(f"Vector store created with {len(self.chunks)} chunks.")
        except Exception as e:
            logging.error(f"Error creating vector store: {e}")
            raise

    def load_vector_store(self):
        """Loads the FAISS index and TF-IDF metadata correctly."""
        try:
            self.index = faiss.read_index(self.faiss_index_path)
            with open(self.metadata_path, "rb") as f:
                self.chunks, self.metadata, self.vectorizer = pickle.load(f)

            logging.info(f"Vector store loaded successfully. Total chunks: {len(self.chunks)}")
            logging.info(f"FAISS index size: {self.index.ntotal}")
        except Exception as e:
            logging.error(f"Error loading vector store: {e}")
            raise

    def retrieve_relevant_text(self, query):
        """Retrieves chunks within a similarity threshold using FAISS."""
        try:
            if self.index.ntotal == 0:
                logging.warning("FAISS index is empty! No data to search.")
                return "No relevant information found."

            query_vector = normalize(self.vectorizer.transform([query]).toarray(), norm='l2').astype(np.float32)
            logging.info(f"Query vector shape: {query_vector.shape}")

            distances, indices = self.index.search(query_vector, k=10)
            logging.info(f"Retrieved distances: {distances}")
            logging.info(f"Retrieved indices: {indices}")

            results = []
            for dist, idx in zip(distances[0], indices[0]):
                similarity = 1 / (1 + dist)
                if similarity >= self.similarity_threshold:
                    chunk_info = self.metadata[idx]
                    results.append(f"📜 {chunk_info['filename']} (Chunk {chunk_info['start_idx']}):\n{self.chunks[idx]}")
                logging.info(f"🔍 Distance: {dist}, Index: {idx}, Chunk: {self.chunks[idx]}")

            return "\n\n".join(results) if results else "No relevant information found."
        except Exception as e:
            logging.error(f"Error retrieving relevant text: {e}")
            return "No relevant information found."

    def interactive_chat(self):
        """Runs an interactive chat loop."""
        try:
            while True:
                user_query = input("\n🔍 Ask about an alert (type 'exit' to quit): ").strip()
                if user_query.lower() in ["exit", "quit"]:
                    break

                relevant_text = self.retrieve_relevant_text(user_query)
                print(f"\n📌 **Relevant Context:**\n{relevant_text}")

        except KeyboardInterrupt:
            logging.info("Program terminated by user.")
        except Exception as e:
            logging.error(f"Unexpected error: {e}")


# Main Execution
if __name__ == "__main__":
    bot = AlertQueryBot()
    bot.interactive_chat()
