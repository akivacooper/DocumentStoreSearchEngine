package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import edu.yu.cs.com1320.project.stage5.Document;

public class DocumentImpl implements Document{

    private URI uri = null;
    private String txt = null;
    private byte[] binaryData;
    private Map<String, Integer> wordMap;
    private long lastUsed;

    public DocumentImpl(URI uri, String txt){
        
        if (uri == null || txt == null || txt.length() == 0 || uri.toString().equals("")){
            throw new java.lang.IllegalArgumentException();
        } 

        this.wordMap = new HashMap<String, Integer>();

        this.uri = uri;
        this.txt = txt;
        makeHashMap();

        lastUsed = System.nanoTime();
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        
        if (uri == null || binaryData == null || binaryData.length == 0 || uri.toString().equals("")){
            throw new java.lang.IllegalArgumentException();
        } 

        this.wordMap = new HashMap<String, Integer>();


        this.uri = uri;
        this.binaryData = binaryData;

        lastUsed = System.nanoTime();
    }

    /**
     * @return content of text document
     */
    public String getDocumentTxt(){
        return this.txt;
    }

    /**
     * @return content of binary data document
     */
    public byte[] getDocumentBinaryData(){
        return binaryData;


    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey(){
        return this.uri;

    }

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    public int wordCount(String word){
        if (this.txt == null){
            return 0;
        } else {
            try {
                return wordMap.get(fixText(word));
            } catch (NullPointerException e){
                return 0;
            }
        }
    }

    /**
     * @return all the words that appear in the document
     */
    public Set<String> getWords(){
        return wordMap.keySet();

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (txt != null ? txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        } else if (obj == null){
            return false;
        } else if (getClass()!=obj.getClass()){
            return false;
        } else if (hashCode()==obj.hashCode()){
            return true;
        } else {
            return false;
        }
    }

    public long getLastUseTime(){
        return lastUsed;
    }
    
    public void setLastUseTime(long timeInNanoseconds){
        lastUsed = timeInNanoseconds;
    }

    @Override
    public int compareTo(Document docToCompare) {
        if (lastUsed > docToCompare.getLastUseTime()) {
            return 1;
        } else if (lastUsed == docToCompare.getLastUseTime()){
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * This method does two things:
     * 1. makes the text case insensitive (lowercase)
     * 2. takes out special characters
     * 
     * This method does NOT take out spaces. 
     * This method only fixes the characters of a given text
     * Taking out spaces is important for the hashmap
     * But the instance variable txt still has the spaces
     * Therefore, we will not take them out here
     * @param txt
     * @return
     */
    private String fixText (String txt){
        return parse(txt.toLowerCase());
    }

    private String parse(String word){
        word = word.toLowerCase();
        return word.replaceAll("[_\\W]+", "");
    }

    private void makeHashMap (){
        String[] words = this.txt.split(" ");
        for (String nextWord : words){
            if (nextWord.equals(" ") || (nextWord.equals(""))){
                continue;
            }
            String newWord = fixText(nextWord);
            //System.out.println("Just added to the hasmap the following word: " + newWord);


            Integer currentWordCount = this.wordMap.get(newWord);
            if (currentWordCount != null){
                this.wordMap.put(newWord, currentWordCount + 1);
            } else {
                this.wordMap.put(newWord, 1);
            }
        }
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.wordMap;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        // TODO Auto-generated method stub
        this.wordMap = wordMap;
        
    }




}