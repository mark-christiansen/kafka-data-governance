package com.mycompany.kafka.governance.data.protection.util;

/**
 * @deprecated
 * The idea of this rule was to evaluate whether a string was encrypted based on how random the characters are in the
 * string by measuring the string's entropy. This is also failed to produce consistent results.
 */
@Deprecated
public class Entropy {

    public static double calculateEntropy(String str) {
        int[] frequencies = findFrequencies(str);
        double[] probabilities = findProbabilities(str, frequencies);
        double entropy = calculateEntropy(probabilities);
        return entropy;
    }

    public static double calculateEntropy(double[] probabilities) {
        double shannonEntropy = 0;
        int n = probabilities.length;
        for (int i = 0; i < n; i++) {
            if (probabilities[i] != 0) {
                shannonEntropy += probabilities[i]*log2(probabilities[i]);
            }
        }
        return -1*shannonEntropy;
    }

    public static String removeUnnecessaryChars(String word) {

        String text = "";
        while (!word.isEmpty()) {

            int wordLength = word.length();
            String newWord = "";
            for (int i = 0; i < wordLength; i++) {
                if (word.charAt(i) != '.' &&
                        word.charAt(i) != '!' &&
                        word.charAt(i) != '?' &&
                        word.charAt(i) != ',' &&
                        word.charAt(i) != '"' &&
                        word.charAt(i) != ':' &&
                        word.charAt(i) != ';' &&
                        word.charAt(i) != '(' &&
                        word.charAt(i) != ')') {
                    newWord += word.charAt(i);
                }
            }
            text += newWord;
        }
        return text.toLowerCase();
    }

    private static int[] findFrequencies(String text) {

        int textLength = text.length();
        /*
        char[] ALPHABET = {'a','b','c','d','e','f','g','h','i','j','k','l',
                           'm','n','o','p','q','r','s','t','u','v','w','x',
                           'y','z'};
        */
        char[] ALPHABET = {'a','c','g','t'}; // specifically used for genes and genomes
        int[] frequencies = new int[ALPHABET.length];
        for (int i = 0; i < textLength; i++) {
            for (int j = 0; j < ALPHABET.length; j++) {
                if (text.charAt(i) == ALPHABET[j]) {
                    frequencies[j]++;
                    break; // to speed up the computation
                }
            }
        }
        return frequencies;
    }

    private static double[] findProbabilities(String text, int[] frequencies) {
        int textLength = text.length();
        int n = frequencies.length;
        double[] probabilities = new double[n];
        for (int i = 0; i < n; i++) {
            probabilities[i] = (double) frequencies[i]/textLength;
        }
        return probabilities;
    }

    private static double log2(double x) {
        return (Math.log(x)/Math.log(2));
    }
}
