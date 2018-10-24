import java.io.*;
import java.util.*;

public class CypherBreaker {
    private Character[] letterKnownFrequency = {'e', 't', 'a', 'o', 'i', 'n', 's', 'h', 'r', 'd', 'l', 'u', 'c', 'm', 'w', 'f', 'g', 'y', 'p', 'b', 'v', 'k', 'j', 'x', 'q', 'z'};
    private Set<String> dictionary = new HashSet<>();

    public CypherBreaker(String encodedText) {
        System.out.println(encodedText);
        Map<Character, Integer> letterFrequency = letterFrequencyAnalysis(encodedText);
        Map<Character, Character> mapping = new HashMap<>();
        int counter = 0;
        while (letterFrequency.keySet().size() > 0) {
            Character mostFrequentChar = null;
            Integer biggestFrequency = 0;
            for (Character c : letterFrequency.keySet()) {
                if (mostFrequentChar == null) {
                    mostFrequentChar = c;
                } else {
                    if (letterFrequency.get(c) > biggestFrequency) {
                        biggestFrequency = letterFrequency.get(c);
                        mostFrequentChar = c;
                    }
                }

            }
            mapping.put(mostFrequentChar, letterKnownFrequency[counter]);
            counter++;
            letterFrequency.remove(mostFrequentChar);
        }
        System.out.println("Here");
        mapping.keySet().forEach(c -> System.out.println(c + " : " + mapping.get(c)));
        System.out.println();
        decode(encodedText, mapping);
        readInDictionary();
    }

    private void decode(String encodedText, Map<Character, Character> mapping) {

        StringBuilder decodedText = new StringBuilder();
        for (int i = 0; i < encodedText.length(); i++) {
            if (mapping.containsKey(encodedText.charAt(i))) {
                decodedText.append(mapping.get(encodedText.charAt(i)));
            } else {
                decodedText.append(encodedText.charAt(i));
            }
        }
        System.out.println(decodedText);
    }

    private Map<Character, Integer> letterFrequencyAnalysis(String encodedText) {
        Map<Character, Integer> letterFrequency = new HashMap<>();
        encodedText = encodedText.replaceAll("\\s+", "");
        for (int i = 0; i < encodedText.length(); i++) {
            Character c = encodedText.charAt(i);
            if (letterFrequency.keySet().contains(c)) {
                letterFrequency.put(c, letterFrequency.get(c) + 1);
            } else {
                letterFrequency.put(c, 1);
            }

        }
//        for (Character c : letterFrequency.keySet()) {
//            System.out.println(c + " : " + letterFrequency.get(c));
//        }
        return letterFrequency;

    }

    private void readInDictionary(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("dictionary.txt").getFile());
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String word;
            while ((word = br.readLine()) != null) {
                dictionary.add(word);
            }
            System.out.println("DONE " + dictionary.size());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
