/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class SentiLexicon {

    public HashMap<String, Integer> dictionary = new HashMap<String, Integer>();

    public SentiLexicon() throws IOException {

        /*
         Η βασική αναπαράσταση του λεξικου, απο String σε λίστα απο double.
         */
        BufferedReader csvPos = null;
        BufferedReader csvNeg = null;
        BufferedReader csvPos2 = null;
        BufferedReader csvNeg2 = null;
        BufferedReader csvNeg3 = null;
        String positive = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/LoughranMcDonald_Positive.csv";
        String positive2 = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/positive-words.txt";
        String negative = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/LoughranMcDonald_Negative.csv";
        String negative3 = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/LoughranMcDonald_Litigious.csv";
        String negative2 = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/negative-words.txt";
        try {
            csvPos = new BufferedReader(new FileReader(positive));
            csvNeg = new BufferedReader(new FileReader(negative));
            csvPos2 = new BufferedReader(new FileReader(positive2));
            csvNeg2 = new BufferedReader(new FileReader(negative2));
            csvNeg3 = new BufferedReader(new FileReader(negative3));
            int lineNumber = 0;

            String line;
            while ((line = csvPos.readLine()) != null) {
                lineNumber++;

                /*
                 Αν είναι σχολιο, μη το χρησιμοποήσεις.
                 */
                if (!line.trim().startsWith("#")) {
                    dictionary.put(line, 1);

                }
            }
            while ((line = csvNeg.readLine()) != null) {
                lineNumber++;

                if (!line.trim().startsWith("#")) {
                    dictionary.put(line, -1);

                }
            }
            
            while ((line = csvPos2.readLine()) != null) {
                lineNumber++;

                /*
                 Αν είναι σχολιο, μη το χρησιμοποήσεις.
                 */
                if (!line.trim().startsWith("#")) {
                    dictionary.put(line, 1);

                }
            }
            while ((line = csvNeg2.readLine()) != null) {
                lineNumber++;

                if (!line.trim().startsWith("#")) {
                    dictionary.put(line, -1);

                }
            }
            
            while ((line = csvNeg3.readLine()) != null) {
                lineNumber++;

                if (!line.trim().startsWith("#")) {
                    dictionary.put(line, -1);

                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csvPos != null) {
                csvPos.close();
            }
            if (csvNeg != null) {
                csvNeg.close();
            }
        }

    }

    public int getScore(String sentence) {
        int sum = 0;
        String[] words = sentence.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (dictionary.get(words[i]) != null) {
                sum += (int) dictionary.get(words[i]);
            }
        }
        return sum;
    }

}
