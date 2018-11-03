import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class CypherBreakerPattern {

    private Set<String> dictionary = new HashSet<>();
    private Map<List<Integer>, List<String>> patternsToWords = new HashMap<>();
    private Map<Character, Character> sureMapping = new HashMap<>();
    private String rawEncodedText;


    CypherBreakerPattern(String encodedText) {
        rawEncodedText = encodedText.toLowerCase();
        readInDictionary();
        String[] encodedMessage = encodedText.toLowerCase().split("\\s+");
        Map<Character, Set<Character>> finalMapping = calculatePossibleMappings(encodedMessage);

        while (true) {
            Map<Character, Set<Character>> newMapping = calculatePossibleMappings(encodedMessage);
            if (finalMapping.equals(newMapping)) {
                break;
            }
            finalMapping = newMapping;
            printLetterMap(finalMapping);
        }
        tryOutKeys(finalMapping, encodedMessage);
    }

    // Decodes the message using given mapping
    private void decode(String encodedText, Map<Character, Character> mapping) {
        System.out.println("Encoded message:              " + encodedText);
        StringBuilder decodedText = new StringBuilder();
        for (int i = 0; i < encodedText.length(); i++) {
            if (mapping.containsKey(encodedText.charAt(i))) {
                decodedText.append(mapping.get(encodedText.charAt(i)));
            } else {
                decodedText.append(encodedText.charAt(i));
            }
        }
        System.out.println("One of the possible decoding: " + decodedText);
    }

    //Reads in possible words
    private void readInDictionary() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("commonwords.txt").getFile());

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String word;
            while ((word = br.readLine()) != null) {
                dictionary.add(word);
                List<Integer> pattern = getWordPattern(word);
                if (patternsToWords.containsKey(pattern)) {
                    patternsToWords.get(pattern).add(word);
                } else {
                    List<String> newList = new LinkedList<>();
                    newList.add(word);
                    patternsToWords.put(pattern, newList);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Returns a word pattern e.g. "good" -> 1,2,2,3
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
        return pattern;
    }

    // Generates possible mapping from characters to set of characters
    private Map<Character, Set<Character>> calculatePossibleMappings(String[] encodedMessage) {
        Map<Character, Set<Character>> finalMapping = new HashMap<>();
        for (String encodedWord : encodedMessage) {
            mapAWord(encodedWord, finalMapping);
        }
        removeLettersFromFinalMap(finalMapping);
        return finalMapping;
    }

    // Takes encrypted word and adds all the possible mappings to each letter to the final map
    private void mapAWord(String encryptedWord, Map<Character, Set<Character>> finalMapping) {
        List<String> possibleWords = patternsToWords.get(getWordPattern(encryptedWord));
        Map<Character, Set<Character>> mapping = new HashMap<>();
        for (int k = 0; k < encryptedWord.length(); k++) {
            if (!mapping.containsKey(encryptedWord.charAt(k))) { //Map did not have mapping to this character just yet.
                mapping.put(encryptedWord.charAt(k), new HashSet<>());
            }
            for (String wordFromDic : possibleWords) {
                if (wordIsFineByKnownLetters(wordFromDic, encryptedWord)) {
                    mapping.get(encryptedWord.charAt(k)).add(wordFromDic.charAt(k));
                }
            }
        }
        addToFinalMapping(mapping, finalMapping);
    }

    // Check if word is possible if known mapping is used
    private boolean wordIsFineByKnownLetters(String wordFromDic, String encryptedWord) {
        for (int i = 0; i < encryptedWord.length(); i++) {
            if (sureMapping.containsKey(encryptedWord.charAt(i))) {
                if (wordFromDic.charAt(i) != sureMapping.get(encryptedWord.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    // Remove chars from final mapping if newMapping cannot have it as a potential mapping
    private void addToFinalMapping(Map<Character, Set<Character>> newMapping, Map<Character, Set<Character>> finalMapping) {
        for (Character c : newMapping.keySet()) {
            if (finalMapping.containsKey(c)) {
                finalMapping.get(c).removeIf(mappedChar -> !newMapping.get(c).contains(mappedChar));
            } else {
                finalMapping.put(c, newMapping.get(c));
            }
        }
    }

    // If letter is mapped to only one key, remove it from all other mappings
    private void removeLettersFromFinalMap(Map<Character, Set<Character>> finalMapping) {
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
    }

    // Uses final map with all the possible mappings and generates keys to try
    private void tryOutKeys(Map<Character, Set<Character>> finalMap, String[] encodedMessage) {

        Random rnd = new Random();
        int maxGoodWords = 0;
        Map<Character, Character> bestMap = null;

        while (true) {
            boolean badKey = false;
            Map<Character, Character> mapToTry = new HashMap<>();
            Set<Character> charsUsed = new HashSet<>();
            for (Character c : finalMap.keySet()) {
                List<Character> possibleChars = new LinkedList<>();
                for (Character possibleChar : finalMap.get(c)) {
                    if (!charsUsed.contains(possibleChar)) {
                        possibleChars.add(possibleChar);
                    }
                }
                if (possibleChars.isEmpty()) {
                    badKey = true;
                    break;
                }
                int randomChar = rnd.nextInt(possibleChars.size());
                if (randomChar > -1) {
                    mapToTry.put(c, possibleChars.get(randomChar));
                    charsUsed.add(possibleChars.get(randomChar));
                } else {
                    badKey = true;
                    break;
                }
            }
            if (!badKey) {
                int goodWords = howManyGoodWords(mapToTry, encodedMessage);
                if (maxGoodWords < goodWords) {
                    maxGoodWords = goodWords;
                    bestMap = mapToTry;
                }
                if (maxGoodWords >= encodedMessage.length) {
                    decode(rawEncodedText, bestMap);
                    for (Character ca : bestMap.keySet()) {
                        System.out.println("Final mapping: " + ca + " maps to " + bestMap.get(ca));
                    }
                    return;
                }
            }
        }
    }

    // Counts how many valid words form using the map
    private int howManyGoodWords(Map<Character, Character> map, String[] encodedMessage) {
        int goodWords = 0;
        for (String word : encodedMessage) {
            StringBuilder decodedText = new StringBuilder();
            for (int k = 0; k < word.length(); k++) {
                if (map.containsKey(word.charAt(k))) {
                    decodedText.append(map.get(word.charAt(k)));
                } else {
                    decodedText.append(word.charAt(k));
                }
            }
            if (dictionary.contains(decodedText.toString())) {
                goodWords++;
            }
        }
        return goodWords;
    }

    // prints possible mapping from letters to set of letters
    private void printLetterMap(Map<Character, Set<Character>> map) {
        double possibleKeys = 1;
        for (Character c : map.keySet()) {
            System.out.println(c + " possible mappings " + map.get(c) + " " + map.get(c).size());
            possibleKeys = possibleKeys * map.get(c).size();
        }
        System.out.println("A rough estimate of possible keys: " + possibleKeys);
        System.out.println();
    }

}
