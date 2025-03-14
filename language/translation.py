import tensorflow as tf
from tensorflow.keras.models import load_model
import numpy as np
import pickle
from tensorflow.keras.preprocessing.sequence import pad_sequences

# Load trained model and tokenizers
model = load_model("seq2seq_translation.h5")
en_tokenizer = pickle.load(open("en_tokenizer.pkl", "rb"))
es_tokenizer = pickle.load(open("es_tokenizer.pkl", "rb"))

# Extract vocabulary sizes
vocab_size_en = len(en_tokenizer.word_index) + 1
vocab_size_es = len(es_tokenizer.word_index) + 1

# Reverse lookup dictionary for Spanish words
reverse_es_tokenizer = {index: word for word, index in es_tokenizer.word_index.items()}

# Extract the encoder model
encoder_inputs = model.input[0]  # Encoder input layer
encoder_outputs, state_h, state_c = model.layers[4].output  # LSTM output and states
encoder_model = tf.keras.Model(encoder_inputs, [state_h, state_c])

# Extract the decoder components from the trained model
decoder_inputs = model.input[1]  # Get the decoder input layer
decoder_embedding_layer = model.get_layer(index=3)  # Get embedding layer
decoder_lstm_layer = model.get_layer(index=5)  # Get LSTM layer
decoder_dense_layer = model.get_layer(index=6)  # Get Dense layer

# Define new input layers with unique names
decoder_state_input_h = tf.keras.Input(shape=(512,), name="decoder_state_h")
decoder_state_input_c = tf.keras.Input(shape=(512,), name="decoder_state_c")
decoder_states_inputs = [decoder_state_input_h, decoder_state_input_c]

# Embed decoder input
decoder_embeddings = decoder_embedding_layer(decoder_inputs)
decoder_outputs, state_h, state_c = decoder_lstm_layer(
    decoder_embeddings, initial_state=decoder_states_inputs
)

# Define decoder output and state update
decoder_states = [state_h, state_c]
decoder_outputs = decoder_dense_layer(decoder_outputs)

# Define the final decoder model
decoder_model = tf.keras.Model(
    inputs=[decoder_inputs] + decoder_states_inputs,
    outputs=[decoder_outputs] + decoder_states,
    name="Decoder_Model"
)



# Tokenization utility function
def tokenize_sentence(sentence, tokenizer, max_len):
    sequence = tokenizer.texts_to_sequences([sentence])
    return pad_sequences(sequence, maxlen=max_len, padding="post")


# Function to translate English to Spanish
def translate_sentence(input_sentence):
    max_len_en = model.input_shape[0][1]  # Max encoder length
    max_len_decoder = model.input_shape[1][1]  # Max decoder length

    # Encode input sentence
    input_seq = tokenize_sentence(input_sentence, en_tokenizer, max_len_en)
    state_values = encoder_model.predict(input_seq)

    # Initialize decoder input sequence with <start> token
    start_token = es_tokenizer.word_index.get("<start>", 1)
    end_token = es_tokenizer.word_index.get("<end>", 2)

    target_seq = np.zeros((1, 1))
    target_seq[0, 0] = start_token

    decoded_sentence = []

    for _ in range(max_len_decoder - 1):
        preds, h, c = decoder_model.predict([target_seq] + state_values, verbose=0)
        next_word_id = np.argmax(preds[0, -1, :])

        if next_word_id == end_token:
            break  # Stop at end token

        decoded_sentence.append(reverse_es_tokenizer.get(next_word_id, "<unk>"))

        # Update the target sequence and decoder states
        target_seq[0, 0] = next_word_id
        state_values = [h, c]

    return " ".join(decoded_sentence)


# Run translations interactively
while True:
    user_input = input("\nEnter English sentence (or 'exit' to stop): ")
    if user_input.lower() == "exit":
        break
    translation = translate_sentence(user_input.lower())
    print(f"🔹 Spanish Translation: {translation}")
