import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CypherBreakerPattern {

    private Set<String> dictionary = new HashSet<>();
    private Map<List<Integer>, List<String>> patternsToWords = new HashMap<>();
    private List<String> encodedMessage = new LinkedList<>();
    private Map<Character, Set<Character>> finalMapping = new HashMap<>();
    private Map<Character, Character> sureMapping = new HashMap<>();


    public CypherBreakerPattern(String encodedText) {
//        sureMapping.put('e', 'e');
//        sureMapping.put('r', 'a');
        readInDictionary();
        String[] encodedMessage = encodedText.toUpperCase().split("\\s+");
        calculatePossibleMappings(encodedMessage);
        calculatePossibleMappings(encodedMessage);
        calculatePossibleMappings(encodedMessage);
        calculatePossibleMappings(encodedMessage);
        calculatePossibleMappings(encodedMessage);
        calculatePossibleMappings(encodedMessage);
//        tryOutKeys();
        System.out.println("encodedMessage size " + encodedMessage.length);
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


    private void readInDictionary() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("dictionary.txt").getFile());
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String word;
            while ((word = br.readLine()) != null) {
//                dictionary.add(word);
                List<Integer> pattern = getWordPattern(word);
                if (patternsToWords.containsKey(pattern)) {
                    patternsToWords.get(pattern).add(word);
                } else {
                    List<String> newList = new LinkedList<>();
                    newList.add(word);
                    patternsToWords.put(pattern, newList);
                }
            }
            System.out.println("DONE " + patternsToWords.keySet().size());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<Integer> getWordPattern(String word) {
        Map<Character, Integer> lettersUsed = new HashMap<>();
        List<Integer> pattern = new LinkedList<>();
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            Character c = word.charAt(i);
            if (lettersUsed.containsKey(c)) {
                pattern.add(lettersUsed.get(c));
            } else {
                lettersUsed.put(c, count);
                pattern.add(count++);
            }
        }
//        System.out.print(word + " pattern ");
//        pattern.forEach(System.out::print);
//        System.out.println();
        return pattern;
    }

    private void calculatePossibleMappings(String[] encodedMessage) {
        for (int i = 0; i < encodedMessage.length; i++) {
            mapAWord(encodedMessage[i]);
        }
        removeLettersFromFinalMap();
        double possibleKeys = 1;
        for (Character c : finalMapping.keySet()) {
            System.out.println(c + " possible mappings " + finalMapping.get(c) + " " + finalMapping.get(c).size());
            possibleKeys = possibleKeys * finalMapping.get(c).size();
        }
        System.out.println("Possible keys: " + +possibleKeys);


    }

    private void mapAWord(String encryptedWord) {
        System.out.println("WORD " + encryptedWord);
        List<String> possibleWords = patternsToWords.get(getWordPattern(encryptedWord));
        Map<Character, Set<Character>> mapping = new HashMap<>();
        System.out.println("______");
        for (int k = 0; k < encryptedWord.length(); k++) {
            if (!mapping.containsKey(encryptedWord.charAt(k))) { //Map didint have mapping to this character just yet.
                mapping.put(encryptedWord.charAt(k), new HashSet<>());
            }
            for (String wordFromDic : possibleWords) {
                if (wordIsFineByKnownLetters(wordFromDic, encryptedWord)) {
//                    System.out.println("COULD be WORD " + wordFromDic);
                    mapping.get(encryptedWord.charAt(k)).add(wordFromDic.charAt(k));
                }
            }
        }
        if (encryptedWord.equals("hennrse")) {
            for (Character c : mapping.keySet()) {
                System.out.println("BINGO       " + c + " possible mappings " + mapping.get(c) + " " + mapping.get(c).size());
            }
        }
        addToFinalMapping(mapping);
    }

    private boolean wordIsFineByKnownLetters(String wordFromDic, String encryptedWord) {
        for (int i = 0; i < encryptedWord.length(); i++) {
            if (sureMapping.containsKey(encryptedWord.charAt(i))) {
                if (wordFromDic.charAt(i) != sureMapping.get(encryptedWord.charAt(i))) {
//                    System.out.println("wordFromDic rejected: " + wordFromDic + " encrypted word " + encryptedWord + " chars " + wordFromDic.charAt(i) + " " + sureMapping.get(encryptedWord.charAt(i)));
                    return false;
                }
            }
        }
//        System.out.println("Accepted: " + wordFromDic + " Encrypted: " + encryptedWord);
        return true;
    }

    private void addToFinalMapping(Map<Character, Set<Character>> newMapping) {
        for (Character c : newMapping.keySet()) {
            if (finalMapping.containsKey(c)) {
//                for (Character mappedChar : newMapping.get(c)) {
//                    if (!finalMapping.get(c).contains(mappedChar)) {
//                        finalMapping.get(c).remove(c);
//                    }
//                }
                Iterator<Character> iterator = finalMapping.get(c).iterator();
                while (iterator.hasNext()) {
                    Character mappedChar = iterator.next();
                    if (!newMapping.get(c).contains(mappedChar)) {
                        iterator.remove();
                    }
                }
//                for (Character mappedChar : finalMapping.get(c)) {
//
//                }
            } else {
                finalMapping.put(c, newMapping.get(c));
            }

        }
    }

    private void removeLettersFromFinalMap() {
        for (Character c : finalMapping.keySet()) {
            if (finalMapping.get(c).size() == 1) {
                Iterator iter = finalMapping.get(c).iterator();
                Character sureChar = (Character) iter.next();
                sureMapping.put(c, sureChar);
                for (Character key : finalMapping.keySet()) {
                    if (!key.equals(c)) {
                        finalMapping.get(key).remove(sureChar);
                    }

                }
            }
        }
        for (Character key : sureMapping.keySet()) {
            System.out.println("Key: " + key + " value: " + sureMapping.get(key));
        }
    }

    private void tryOutKeys(String[] encodedMessage) {
        String[] decodedMessage = new String[encodedMessage.length];


    }

    private boolean isInDic(String word) {
        return dictionary.contains(word);
    }

}
