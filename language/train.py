import tensorflow as tf
from tensorflow.keras.layers import Embedding, LSTM, Dense, Input
from tensorflow.keras.models import Model
import numpy as np
import nltk
from nltk.tokenize import word_tokenize
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
import pickle

# Download NLTK tokenizer
nltk.download('punkt')

# Load Data
def load_data(en_file, es_file):
    with open(en_file, "r", encoding="utf-8") as f:
        en_lines = f.read().strip().split("\n")
    with open(es_file, "r", encoding="utf-8") as f:
        es_lines = f.read().strip().split("\n")
    return en_lines, es_lines

# Load sentence pairs
en_sentences, es_sentences = load_data("english.txt", "spanish.txt")

# Add <start> and <end> tokens to Spanish sentences
es_sentences = [f"<start> {sent} <end>" for sent in es_sentences]

# Tokenization function
def tokenize(sentences):
    tokenizer = Tokenizer(filters='', oov_token="<unk>")  # Handle unknown words
    tokenizer.fit_on_texts(sentences)
    sequences = tokenizer.texts_to_sequences(sentences)
    return sequences, tokenizer

# Tokenize data
en_sequences, en_tokenizer = tokenize(en_sentences)
es_sequences, es_tokenizer = tokenize(es_sentences)

# Find max sequence length for padding
max_len_en = max(len(seq) for seq in en_sequences)
max_len_es = max(len(seq) for seq in es_sequences)

# Use the longer max length
max_len = max(max_len_en, max_len_es)

# Padding sequences
en_sequences = pad_sequences(en_sequences, maxlen=max_len, padding="post")
es_sequences = pad_sequences(es_sequences, maxlen=max_len, padding="post")

# Decoder input & target preparation (teacher forcing)
decoder_input_data = es_sequences[:, :-1]  # Shift left
decoder_target_data = es_sequences[:, 1:]  # Shift right

# Ensure both have the same length
decoder_input_data = pad_sequences(decoder_input_data, maxlen=max_len - 1, padding="post")
decoder_target_data = pad_sequences(decoder_target_data, maxlen=max_len - 1, padding="post")

# Vocabulary sizes
vocab_size_en = len(en_tokenizer.word_index) + 1
vocab_size_es = len(es_tokenizer.word_index) + 1

# Model Hyperparameters
embedding_dim = 256
hidden_units = 512

# Encoder
encoder_inputs = Input(shape=(max_len,))
encoder_embedding = Embedding(vocab_size_en, embedding_dim)(encoder_inputs)
encoder_lstm = LSTM(hidden_units, return_state=True)
encoder_outputs, state_h, state_c = encoder_lstm(encoder_embedding)
encoder_states = [state_h, state_c]

# Decoder
decoder_inputs = Input(shape=(max_len - 1,))
decoder_embedding = Embedding(vocab_size_es, embedding_dim)(decoder_inputs)
decoder_lstm = LSTM(hidden_units, return_sequences=True, return_state=True)
decoder_outputs, _, _ = decoder_lstm(decoder_embedding, initial_state=encoder_states)
decoder_dense = Dense(vocab_size_es, activation="softmax")
decoder_outputs = decoder_dense(decoder_outputs)

# Define full model
model = Model([encoder_inputs, decoder_inputs], decoder_outputs)
model.compile(optimizer="adam", loss="sparse_categorical_crossentropy", metrics=["accuracy"])

# Train model
model.fit([en_sequences, decoder_input_data], decoder_target_data, batch_size=64, epochs=50)

# Save model and tokenizers
model.save("seq2seq_translation.h5")
pickle.dump(en_tokenizer, open("en_tokenizer.pkl", "wb"))
pickle.dump(es_tokenizer, open("es_tokenizer.pkl", "wb"))

print("✅ Model training complete and saved!")
