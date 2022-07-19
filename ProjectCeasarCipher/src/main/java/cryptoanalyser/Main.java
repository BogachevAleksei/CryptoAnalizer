package cryptoanalyser;


import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import static java.lang.Character.isLetter;
import static java.lang.Character.toLowerCase;

//src/chisquare/commons-math3-3.6.1/commons-math3-3.6.1.jar
public class Main {

    private static final String ALPHABET = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnoprstuvwxz,./@'!_-=+;][{}1234567890 ";
    public static String inputFile = "input.txt";
    public static String encryptFile = "encrypt.txt";
    public static String decryptFile = "decrypt.txt";
    public static String dictionary = "dictionary.txt";


    public static void main(String[] args) throws IOException {

        new FileWriter(decryptFile).close();
        System.out.println("File decrypt.txt in the project directory has been cleared");
        Menu menu = new Menu();
        menu.run();
    }

    static char encrypt(char symbol, int shift) {
        if (isLetter(symbol)) symbol = toLowerCase(symbol);
        if (ALPHABET.indexOf(symbol) != -1) {
            return ALPHABET.charAt((ALPHABET.indexOf(symbol) + shift) % ALPHABET.length());
        } else {
            return symbol;
        }
    }

    static char decrypt(char symbol, int shift) {
        if (isLetter(symbol)) symbol = toLowerCase(symbol);
        return encrypt(symbol, ALPHABET.length() - (shift % ALPHABET.length()));
    }


    static void writeToFile(String filename, String data) {
        Path path = Paths.get(filename);
        try {
            Files.writeString(path, data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int bruteForce(String data) {
        int[] count = new int[ALPHABET.length()];
        int key = 0;
        int max = 0;

        for (int i = 0; i < ALPHABET.length(); i++) {
            List<Character> characters = new ArrayList<>();
            for (int k = 0; k < data.length(); k++) {
                characters.add(decrypt(data.charAt(k), i));
            }
            for (int j = 0; j < characters.size() - 1; j++) {
                if ((characters.get(j).equals('.') && characters.get(j + 1).equals(' ')) || (characters.get(j).equals(',') && characters.get(j + 1).equals(' '))) {
                    count[i]++;
                }
            }
            characters.clear();
        }

        for (int i = 0; i < count.length; i++) {
            if (count[i] > max) {
                max = count[i];
                key = i;
            }
        }
        return key;
    }

    private static long countLetter(char letter, String textFromFile) {
        return textFromFile.chars()
                .filter(character -> character == letter)
                .count();
    }

    private static long[] observedLettersFrequencies(String textFromFile) {
        return IntStream.rangeClosed('а', 'я')
                .mapToLong(letter -> countLetter((char) letter, textFromFile))
                .toArray();
    }

    static double[] chiSquares(String encryptFile, String dictionary) throws IOException{
        String dictionaryText = "";
        try {
            dictionaryText = Files.readString(Path.of(dictionary), StandardCharsets.UTF_8).toLowerCase();

        } catch (IOException e) {
            e.printStackTrace();
        }

        long[] expectedLettersFrequencies = observedLettersFrequencies(dictionaryText);
        double[] doubleExpLetterFreq = new double[expectedLettersFrequencies.length];
        for (int i = 0; i < expectedLettersFrequencies.length; i++) {
            doubleExpLetterFreq[i] = (double) expectedLettersFrequencies[i];
        }

        double[] chiSquares = new double[ALPHABET.length()];

        for (int shift = 0; shift < chiSquares.length; shift++) {
            try (FileInputStream inputStream = new FileInputStream(encryptFile);
                 Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                StringBuilder stringBuilder = new StringBuilder();
                scanner.useDelimiter("");
                while (scanner.hasNext()) {
                    stringBuilder.append(decrypt(scanner.next().charAt(0), shift));
                }
                long[] lettersFrequencies = observedLettersFrequencies(stringBuilder.toString());
                double chiSquare = new ChiSquareTest().chiSquare(doubleExpLetterFreq, lettersFrequencies);
                chiSquares[shift] = chiSquare;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Something went wrong");
            }
        }
        return chiSquares;
    }

    static int statShift(double[] chiSquares) {
        int statShift = 0;
        for (int shift = 0; shift < chiSquares.length; shift++) {
            if (chiSquares[shift] < chiSquares[statShift]) {
                statShift = shift;
            }
        }
        return statShift;
    }
}


