package edu.yu.cs.com1320.project.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import edu.yu.cs.com1320.project.Trie;

/**
 * FOR STAGE 3
 * @param <Value>
 */
public class TrieImpl<Value> implements Trie<Value>
{

    //Node class
    class Node<Value>
    {
        Set<Value> nodeValueSet = new HashSet<Value>();
        Node[] LinkArray = new Node[TrieImpl.alphabetSize];
    }

    //Instance Variable
    private static final int alphabetSize = 36;
    private Node root; // root of trie
    private Set<Value> relevantSet = new HashSet<Value>();

    //Constructor
    public TrieImpl (){
    }

    //--------------------------------------------------------------------------
    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    public void put(String key, Value val){
        if (key == null){
            throw new IllegalArgumentException();
        } else if (key.equals("") || val == null){
            return;
        } else {
            key = key.toLowerCase();
            this.root = put(this.root, key, val, 0);
        }
    }
    
    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values, in descending order
     */
    public List<Value> getAllSorted(String key, Comparator<Value> comparator){
        if (key == null || comparator == null){
            throw new IllegalArgumentException();
        } else if (key.equals("")){
            return new ArrayList<Value>();
        } else {
            key = key.toLowerCase();

            /** 
            System.out.println();
            System.out.println("In trie");
            System.out.println("Value of the keyword: " + key);
            */
        

            Node wantedNode = getNode(this.root, key, 0); //This will return null if the inputed key leads to a node that doesn't exist
            if (wantedNode != null){
                
                /**
                System.out.println();
                System.out.println("Comparing documents");
                
                System.out.println("The size of the set at the node is: " + wantedNode.nodeValueSet.size());
                */
                return sortSet((HashSet)wantedNode.nodeValueSet, comparator);
            } else {
                return new ArrayList<Value>();
            }
            
            
            /**
             * The node's nodeValueSet is a Set. 
             * This means that it is not organized.
             * Also, it cannot be organized until we know how to organize it
             * Which can only be done now, with the comparator here
             * UNDONE: Organize wantedNode's nodeValueSet
             */
        }


    }
    
    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
      
        /**
        System.out.println();
        System.out.println("*** IN TRIE ***");
        System.out.println();
        System.out.println("Public: Finding sorted list of all documents with a prefix");
        System.out.println();
        */

        if (prefix == null || comparator == null){
            throw new IllegalArgumentException();
        } else if (prefix.equals("")){
            return new ArrayList<Value>();
        } else {

            //System.out.println("Inputed prefix: " + prefix);
        
            prefix = prefix.toLowerCase();

            //System.out.println("Fixed prefix: " + prefix);

            //System.out.println("Relevant Set Size: " + relevantSet.size());
            resetRelevantSet(0);
            //System.out.println("Relevant Set Size: " + relevantSet.size());

            //System.out.println();
            //System.out.println("Private: Finding unsorted list of all documents with a prefix\n");
            getAllWithPrefixUnSorted(this.root, prefix, 0);
            /**
             * RelevantSet is a Set. 
             * This means that it is not organized.
             * Also, it cannot be organized until we know how to organize it
             * Which can only be done now, with the comparator here
             */
            return sortSet(relevantSet, comparator);
        }
    }
   
    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAll(String key){
        if (key == null){
            throw new IllegalArgumentException();
        } else if (key.equals("")){
            return new HashSet<Value>();
        } else {

            key = key.toLowerCase();

            deleteAll(this.root, key, 0);

            return relevantSet;
            /**
             * The node's nodeValueSet is a Set. 
             * This means that it is not organized.
             * Also, it cannot be organized until we know how to organize it
             * Which can only be done now, with the comparator here
             * UNDONE: Organize wantedNode's nodeValueSet
             */

        }
    }
    
    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    public Value delete(String key, Value val){
        resetRelevantSet(0);
        
        //System.out.println("Recieved the word " + key + " in the Trie");


        if (val == null || key == null){
            throw new IllegalArgumentException();
        } else if (key.equals("")){
            //System.out.println("Returned null");
            return null;
        } else {
            key = key.toLowerCase();
            //System.out.println("The word is now: " + key);
            this.delete(this.root, key, val, 0);
            
            if (relevantSet.size() == 1){
                return (Value)relevantSet.toArray()[0];
            } else {
                return null;
            }
        }
    }
    
    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAllWithPrefix(String prefix){

        if (prefix == null){
            throw new IllegalArgumentException();
        } else if (prefix.equals("")) {
            return new HashSet<Value>();
        } else {
            resetRelevantSet(0);
            prefix = prefix.toLowerCase();
            deleteAllWithPrefix(this.root, prefix, 0);
            return relevantSet;
        }
        
    }
    
    //--------------------------------------------------------------------------    
    
    /**
     * 
     * @param currentNode
     * @param key
     * @param val
     * @param count
     * @return the root
     */
    private Node<Value> put(Node<Value> currentNode, String key, Value val, int count){

        key = key.toLowerCase();

        //create a new node
        if (currentNode == null) {
            currentNode = new Node<Value>();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (count == key.length())
        {
            currentNode.nodeValueSet.add(val);
            return currentNode;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        int c = findIndex(key.charAt(count));

        currentNode.LinkArray[c] = this.put(currentNode.LinkArray[c], key, val, count + 1);
        return currentNode;
    }
    
    private Node<Value> deleteAllWithPrefix(Node currentNode, String prefix, int count){

        if (currentNode == null){
            return null;
        }

        if (count == prefix.length()){
            for (int i = 0; i < alphabetSize; i++){
                deleteAllWithPrefix(currentNode.LinkArray[i], prefix, count);
            }
            for(Object nextValue : currentNode.nodeValueSet){
                relevantSet.add((Value)nextValue);
            }
            currentNode.nodeValueSet.clear();
        } else { //continue down the trie to the target node
            int c = findIndex(prefix.charAt(count));
            currentNode.LinkArray[c] = this.deleteAllWithPrefix(currentNode.LinkArray[c], prefix, count + 1);
        }

        return checkNode(currentNode);
        

    }
    
    //returns an array list
    private List<Value> sortSet(Set<Value>setToSort, Comparator<Value> comparator){
        
        List<Value> sortedList = new ArrayList<Value>(); 

        for (Value nextValue : setToSort){
            sortedList.add(nextValue);
            //System.out.println(nextValue.toString());
        }

        for(int i = 1; i < sortedList.size(); i++) {
            Value valueToInsert = sortedList.get(i);

            /**
             * shift any values higher than [nextValue] that are 
             * currently at lower indices one space rightward, 
             * making room for the value in its proper place (we 
             * have space to do so since we "removed" the value from //data[i])
             **/
            int k;
            
            for(k=i; k > 0 && comparator.compare(sortedList.get(k-1), valueToInsert) == 1; k--){
                //“move”, i.e. copy, data[k-1]
                Value elementTwo = sortedList.get(k-1);
                sortedList.set(k, elementTwo); //“move”, i.e. copy, data[k-1]
            }
            
            //put the value in its correct place
            sortedList.set(k, valueToInsert);
        }

        return sortedList;
    }
    
    private void resetRelevantSet(int count){
        //System.out.println("Resetting relevant set");
        
        if (count == 0){
            relevantSet = new HashSet<Value>();
        }
    }

    private Node<Value> delete(Node currentNode, String key, Value val, int count){        

        if (currentNode == null){
            //System.out.println("\nFound a null node\n");
            return null;
        }
        //we're at the node to del - set the val to null
        if (count == key.length()) {
           /**
            System.out.println();
            System.out.println("Found the node which represents the string to delete the value at");
            System.out.println("There are " + currentNode.nodeValueSet.size() + " values at this node before removal.\n");
            */
            if (currentNode.nodeValueSet.remove(val) == true){
                relevantSet.add(val);
            }
            //System.out.println("There are " + currentNode.nodeValueSet.size() + " values at this node after removal.");
        } else { //continue down the trie to the target node
            int c = findIndex(key.charAt(count));
            currentNode.LinkArray[c] = this.delete(currentNode.LinkArray[c], key, val, count + 1);
        }
       
        return checkNode(currentNode);
    }
    
    private Node<Value> checkNode (Node<Value> nodeToCheck){
        if (nodeToCheck.nodeValueSet.size() != 0){
            return nodeToCheck;
        }
        
        //remove subtrie rooted at x if it is completely empty	
        for (int i = 0; i < TrieImpl.alphabetSize; i++) {
            if (nodeToCheck.LinkArray[i] != null) {
                return nodeToCheck; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;

    }

    private Node<Value> deleteAll(Node<Value> currentNode, String key, int count){
        resetRelevantSet(count);
        if (currentNode == null){
            return null;
        }
        //we're at the node to del - set the val to null
        if (count == key.length())
        {
            for (Object nextValue : currentNode.nodeValueSet){
                relevantSet.add((Value)nextValue);
            }
            currentNode.nodeValueSet.clear();
        } else { //continue down the trie to the target node
            int c = findIndex(key.charAt(count));
            currentNode.LinkArray[c] = this.deleteAll(currentNode.LinkArray[c], key, count + 1);
        }
       
        return checkNode(currentNode);
    }

    private int findIndex (char letter){
        //System.out.println("Finding index of inputed: " + letter);
        int index = 0;
        if (letter >= 48 && letter <= 57){
                index = (char) (letter - 48);
        } else if (letter >= 97 && letter <= 122) {
            index = (char) (letter - 87);
        }

        //System.out.println("Index of letter is: " + index);

        return index;
    }

    private void getAllWithPrefixUnSorted(Node<Value> currentNode, String prefix, int count){
        //System.out.println("The count is: " + count);

        if (currentNode == null){
            //System.out.println("currentNode was null");
            return;
        }

        if (count == prefix.length())
        {
            //System.out.println("Found node of prefix: " + prefix);

            //System.out.println("+++Adding all documents at the node: " + currentNode +"+++");
            //System.out.println("Relevant set is at the size: " + relevantSet.size());
            for (Object nextValue : currentNode.nodeValueSet){
                relevantSet.add((Value)nextValue);
            }

            //System.out.println("Relevant set is at the size: " + relevantSet.size());
            //System.out.println("+++Finished adding all documents at the node: " + currentNode +"+++");
            //System.out.println();
            //System.out.println("---Will now enter this node's children---");
            
            for (int i = 0; i < alphabetSize; i++){
                //System.out.println();
                //System.out.println("Entering it's " + (i + 1) + " child");
                getAllWithPrefixUnSorted(currentNode.LinkArray[i], prefix, count);
            }

            //System.out.println("Back at parent");
            return;
        }
        
        //System.out.println("Not yet at correct Node of: " + prefix);
        //System.out.println("Next letter is: " + prefix.charAt(count));
        //before we have found the prefix
        int c = findIndex(prefix.charAt(count));
        //System.out.println("Index of next letter is: " + c +"\n");
        this.getAllWithPrefixUnSorted(currentNode.LinkArray[c], prefix, count + 1);

        //System.out.println();
        //System.out.println("The count is: " + count);
        
    }
    
    private Node<Value> getNode(Node currentNode, String key, int count){
        /**
         * There are two ways for a search to 'miss'
         * 1. All the links exist, but there is no value at the wanted Node
         * 2. A required link is null
         * Here, we only care for two. Only then will we return null. 
         * But where we can, if a node exists, we want to return it
         * Therefore, if the node exists, but doesn't have a nodeValueSet, we will still return the node
         * This if statement, will return null if the node passed in is null, or if the wantedNode does not exist (meaning there aren't the requisite links to reach it)
         */
        if (currentNode == null){
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (count == key.length())
        {
            return currentNode;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        int c = findIndex(key.charAt(count));
        return this.getNode(currentNode.LinkArray[c], key, count + 1);
    }
    
    
    
    
}