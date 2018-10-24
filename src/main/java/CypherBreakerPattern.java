import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CypherBreakerPattern {

    private Set<String> dictionary = new HashSet<>();
    private Map<List<Integer>, List<String>> patternsToWords = new HashMap<>();
    private Map<Character, Character> sureMapping = new HashMap<>();
    private String rawEncodedText;


    public CypherBreakerPattern(String encodedText) {
        rawEncodedText = encodedText;
//        sureMapping.put('e', 'e');
//        sureMapping.put('r', 'a');
        readInDictionary();
        String[] encodedMessage = encodedText.toUpperCase().split("\\s+");
        Map<Character, Set<Character>> finalMapping = calculatePossibleMappings(encodedMessage);
        while (!finalMapping.equals(calculatePossibleMappings(encodedMessage))) {
            finalMapping = calculatePossibleMappings(encodedMessage);
        }
        tryOutKeys(finalMapping, encodedMessage);
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
        System.out.println("DECODED: " + decodedText);
    }


    private void readInDictionary() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("dictionary.txt").getFile());
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

    private Map<Character, Set<Character>> calculatePossibleMappings(String[] encodedMessage) {
        Map<Character, Set<Character>> finalMapping = new HashMap<>();
        for (int i = 0; i < encodedMessage.length; i++) {
            mapAWord(encodedMessage[i], finalMapping);
        }
        removeLettersFromFinalMap(finalMapping);
        double possibleKeys = 1;
        for (Character c : finalMapping.keySet()) {
            System.out.println(c + " possible mappings " + finalMapping.get(c) + " " + finalMapping.get(c).size());
            possibleKeys = possibleKeys * finalMapping.get(c).size();
        }
        System.out.println("Possible keys: " + +possibleKeys);

        return finalMapping;
    }

    private void mapAWord(String encryptedWord, Map<Character, Set<Character>> finalMapping) {
//        System.out.println("WORD " + encryptedWord);
        List<String> possibleWords = patternsToWords.get(getWordPattern(encryptedWord));
        Map<Character, Set<Character>> mapping = new HashMap<>();
//        System.out.println("______");
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
        addToFinalMapping(mapping, finalMapping);
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

    private void addToFinalMapping(Map<Character, Set<Character>> newMapping, Map<Character, Set<Character>> finalMapping) {
        for (Character c : newMapping.keySet()) {
            if (finalMapping.containsKey(c)) {
                Iterator<Character> iterator = finalMapping.get(c).iterator();
                while (iterator.hasNext()) {
                    Character mappedChar = iterator.next();
                    if (!newMapping.get(c).contains(mappedChar)) {
                        iterator.remove();
                    }
                }
            } else {
                finalMapping.put(c, newMapping.get(c));
            }

        }
    }

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
//        for (Character key : sureMapping.keySet()) {
//            System.out.println("Key: " + key + " value: " + sureMapping.get(key));
//        }
    }

    private void tryOutKeys(Map<Character, Set<Character>> finalMap, String[] encodedMessage) {

        int maxGoodWords = 0;
        Map<Character, Character> bestMap;

        for (Character c : finalMap.keySet()) {
            for (Character mapping : finalMap.get(c)) {
                Map<Character, Character> mapToTry = new HashMap<>();
                mapToTry.put(c, mapping);
                for (Character c2 : finalMap.keySet()) {
                    if (c2 != c) {
                        for (Character mapping2 : finalMap.get(c2)) {
                            mapToTry.put(c2, mapping2);
                        }
                    }
                }
                System.out.println("OPAA");
                if (maxGoodWords < howManyGoodWords(mapToTry, encodedMessage)) {
                    maxGoodWords = howManyGoodWords(mapToTry, encodedMessage);
                    bestMap = mapToTry;
                    decode(rawEncodedText, bestMap);
                }
                if (maxGoodWords > 5) return;

            }
        }
    }

    private int howManyGoodWords(Map<Character, Character> map, String[] encodedMessage) {
        int goodWords = 0;
        for (int i = 0; i < encodedMessage.length; i++) {
            String word = encodedMessage[i];
            StringBuilder decodedText = new StringBuilder();
            for (int k = 0; k < word.length(); k++) {
                if (map.containsKey(word.charAt(k))) {
                    decodedText.append(map.get(word.charAt(k)));
                } else {
                    decodedText.append(word.charAt(k));
                }
            }

            if (dictionary.contains(decodedText.toString())) {
                System.out.println("WHY " + decodedText.toString());
                goodWords++;
            }
        }
        return goodWords;
    }
//        for(Character c : finalMap.keySet()){
//            for(Character possibleChar){
//                for(int i = 0; i<encodedMessage.length;i++){
//                    decodedMessage[i] = encodedMessage[i].replace()
//                }
//            }
//        }


    private boolean isInDic(String word) {
        return dictionary.contains(word);
    }

}
