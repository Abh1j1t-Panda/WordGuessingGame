import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class WordGuessingGame {

    private static String secretWord;
    private static char[] guessedLetters;
    private static int attemptsLeft;
    private static final int MAX_ATTEMPTS = 10;
    private static final String RANDOM_WORD_API_URL = "https://random-word-api.herokuapp.com/word"; // Public API

    public static void main(String[] args) {
        try {
            secretWord = fetchRandomWord();
            if (secretWord == null || secretWord.isEmpty()) {
                System.out.println("Failed to fetch a word from the API. Using a default word.");
                secretWord = getRandomFallbackWord(); // Fallback to a local word
            }
        } catch (IOException e) {
            System.err.println("Error fetching word: " + e.getMessage());
            secretWord = getRandomFallbackWord(); // Fallback to a local word if API fetch fails
        }

        guessedLetters = new char[secretWord.length()];
        Arrays.fill(guessedLetters, '_');
        attemptsLeft = MAX_ATTEMPTS;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Word Guessing Game!");
        System.out.println("I have selected a secret word with " + secretWord.length() + " letters.");
        System.out.println("You have " + attemptsLeft + " attempts to guess it.");

        while (attemptsLeft > 0 && !isWordGuessed()) {
            displayGameStatus();
            System.out.print("Guess a letter: ");
            String guessInput = scanner.nextLine().trim().toLowerCase();

            if (!isValidGuess(guessInput)) {
                System.out.println("Invalid input. Please enter a single letter (a-z).");
                continue;
            }

            char guessedChar = guessInput.charAt(0);
            attemptsLeft--;
            processGuess(guessedChar);
        }

        scanner.close(); // Close the scanner to prevent resource leaks.

        if (isWordGuessed()) {
            System.out.println("\nCongratulations! You guessed the word: " + secretWord);
        } else {
            System.out.println("\nGame over! You ran out of attempts. The secret word was: " + secretWord);
        }
    }

    // Method to fetch a random word from the API
    private static String fetchRandomWord() throws IOException {
        URL url = new URL(RANDOM_WORD_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("[") && line.endsWith("]")) {
                // Clean up the response:  "[\\"example\\"]"  -> "example"
                line = line.substring(2, line.length() - 2); // Remove brackets and quotes
                return line.trim();
            }
            return null; // Handle the case where the API returns an unexpected format
        } finally {
            connection.disconnect(); // Ensure the connection is closed
        }
    }

    // Fallback method to get a random word from a local array
    private static String getRandomFallbackWord() {
        String[] fallbackWords = {"apple", "banana", "cherry", "date", "elderberry", "fig", "grape", "kiwi", "lemon", "mango"};
        Random random = new Random();
        return fallbackWords[random.nextInt(fallbackWords.length)];
    }

    private static boolean isValidGuess(String guess) {
        return guess.length() == 1 && Character.isLetter(guess.charAt(0));
    }

    private static void processGuess(char guessedChar) {
        boolean found = false;
        for (int i = 0; i < secretWord.length(); i++) {
            if (secretWord.charAt(i) == guessedChar) {
                guessedLetters[i] = guessedChar;
                found = true;
            }
        }
        if (!found) {
            System.out.println("Incorrect guess.");
        }
    }

    private static void displayGameStatus() {
        System.out.println("\nCurrent progress: " + String.valueOf(guessedLetters));
        System.out.println("Attempts remaining: " + attemptsLeft);
        int correctChars = 0;
        int correctPositions = 0;

        for (int i = 0; i < secretWord.length(); i++) {
            if (guessedLetters[i] != '_') {
                correctChars++;
                if (guessedLetters[i] == secretWord.charAt(i)) {
                    correctPositions++;
                }
            }
        }
        System.out.println("Correct letters: " + correctChars);
        System.out.println("Correct letters in correct positions: " + correctPositions);
    }

    private static boolean isWordGuessed() {
        return String.valueOf(guessedLetters).equals(secretWord);
    }
}