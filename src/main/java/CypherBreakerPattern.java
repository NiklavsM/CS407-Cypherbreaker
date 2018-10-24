import javax.print.DocFlavor;
import javax.swing.text.html.HTMLDocument;
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
        rawEncodedText = encodedText.toUpperCase();
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
        System.out.println("ENCODED: " + encodedText);
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
    }

    private void tryOutKeys(Map<Character, Set<Character>> finalMap, String[] encodedMessage) {

        Random rnd = new Random();

        int maxGoodWords = 0;
        Map<Character, Character> bestMap;
        int keyCounter = 0;
        Map<Character,Integer> charIterator = new HashMap<>();
//        for(Character c : finalMap.keySet()){
//            charIterator.put(c,0);
//        }

        while (keyCounter < 1000000000) {
            boolean badKey = false;
            Map<Character, Character> mapToTry = new HashMap<>();
            Set<Character> charUsed = new HashSet<>();
            for (Character c : finalMap.keySet()) {
                Character[] possibleChars = new Character[finalMap.get(c).size()];
                Iterator<Character> iter = finalMap.get(c).iterator();
                int k = 0;
                while (iter.hasNext()) {
                    Character possibleChar = iter.next();
//                    System.out.println("possibleChar:  " + possibleChar +  " "+ charUsed.contains(possibleChar));
                    if (!charUsed.contains(possibleChar.charValue())) {
                        possibleChars[k] = possibleChar;
                        k++;
                    }
                }
                int randomChar = rnd.nextInt(possibleChars.length);
                keyCounter++;
                if (randomChar > -1 && possibleChars[randomChar] != null) { // possibleChars[randomChar] != null FIX need to check out where null appears
                    mapToTry.put(c, possibleChars[randomChar]);
                    charUsed.add(possibleChars[randomChar]);
                } else {
//                    System.out.println("CHAR " + c);
                    badKey = true;
                }
            }
            if (!badKey) {
                int goodWords = howManyGoodWords(mapToTry, encodedMessage);
                if (maxGoodWords < goodWords) {
                    System.out.println("HERE2");
                    maxGoodWords = goodWords;
                    bestMap = mapToTry;
                    decode(rawEncodedText, bestMap);
                    for (Character ca : bestMap.keySet()) {
                        System.out.println("BesMap       " + ca + " possible mappings " + bestMap.get(ca) + " maxGoodWords " + maxGoodWords);
                    }
                }
                if (maxGoodWords > 15) {
                    System.out.println("KEYCOUNTER " + keyCounter);
                    return;
                }
            }
        }


        System.out.println("KEYCOUNTER " + keyCounter);

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
//                System.out.println("WHY " + decodedText.toString());
                goodWords++;
            }
        }
        return goodWords;
    }

}
