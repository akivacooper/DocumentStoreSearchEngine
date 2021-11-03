package edu.yu.cs.com1320.project.stage5.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

import javax.naming.NotContextException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.CommandSet;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.impl.BTreeImpl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;

public class DocumentStoreImpl implements DocumentStore{
    /**
     * the two document formats supported by this document store.
     * Note that TXT means plain text, i.e. a String.
     */

    private Stack<Undoable> commandStack;
    private Trie<URI> documentTrie;
    private MinHeap<DocRep> documentMemoryHeap;
    private Set<URI> urisInHeap;
    private DocumentPersistenceManager pm;
    private BTree<URI, Document> storageBTree;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private boolean maxDocCountSet;
    private boolean maxDocByteSet;
    private int documentsInHeap;
    private int documentBytesInHeap;

    /**
     * TODO:
     * x Go through the undo logic. Make sure times are being updated appropriately
     * x All undo logic must now also deal with moving things to/from disk in the BTree
     * x Remove any reference to HashTableImpl
     * x Add implementation for BTree
     * x When there isn't enough memory, you should be doing three things:
        * x 1: Remove it from the minHeap (which I believe you're already doing)
        * x 2: In stage4, you deleted it from the HashMap and the Trie. Now, there is no HashMap, so you don't need to worry abou that. Regarding the Trie, you should keep it there, as we can still search the stored documents with their URIs, see @470 and @585_f3. (Searching the trie for stored documents, which, again, is possible, will bring them back to memroy. This means you need to manage memory for a search as well. More on that later)
        * x 3: Tell the BTree to move the document to memory 
     * x Because the Trie now returns URIs that be of documents stored away, when you call the BTree to get the actual documents, you need to be updating some things:
        * 1. The documents returned may have been removed from memory, and the limits of memory may now be exceeded. Therefore, manage memory
        * 2. The documents returned may currently be in the heap, or they may not be (say if their memory was just restored). If they were just restored from memory, you need to add them back to the heap. (It seems like the BTree should be taking care of this part, but I'm unsure how... See @585)
     * I therefore described how we need to manage memory even when doing a search.
     * This issue should be pervasive for this stage, as no structure actually has a document. Therefore, any time a data structure needs a document, we should have to manage memory. 
     * This is not so:
        * Reheapafy: all the documents in the heap are known to be on the disk
        * Trie: we already dealt with
        * Stack: this doesn't actually call BTree. This should still be managing memory, yes
     * x No data structure, other than the BTree, should have a direct reference to a document object, only the URI. It should call the BTree with the URI, whenever any part needs a document
     * x See @472. MinHeap now can't store docs. So how does it compare them. I'm pretty sure you need to make a new inner class here, and send those objects to the minheap. In footnote 3, Mayer seems to indicate this. But other footnotes indicate that the inner class is in teh min heap class, which doesn't make sense to me. 
        * I'd say. Make an object here. Call it filler object. All it stores is the uri of a document. There is also a compareTo method, that takes the URI and the passed in URI, finds each in the BTree, compares the times, and spits out a 1, 0, or -1. 
        * So, you call this filler object whenever you add a document to the heap
        * The filler object gets deleted when it is deleted from the heap
        * When the Heap compares two things, it will call fillerObject.compareTo(compFillerObject).
        * The compareTo method will take its own URI, and the URI of the compFillerObject and find the references in the BTree, and compare the two
        * Now you may be worried. Say a filler object is passed in that is stored away.
        * Say you're comparing a document that is memory, and one that isn't.
        * That's not fair, nor does it make sens. 
        * The document is going to be fetched in the BTree, and it will be brought back to memory
        * Not so. The filler objects only exist in the heap.
        * When deleting documents, you call heap.remove(). 
        * The filler object is returned, and it is deleted from the heap.
        * You use the filler object to get the uri and transport the actual document to storage (with the uri)
        * My point is, when a document is in storage, the corresponding filler object is no longer in the heap
        * and because the filler objects only exist in the heap, you don't need to be worried about documents unfairly being brought back  
     * v Documents will be stored in a directory. 
     * v If the application directory is to be used, the user will call the standard constructor, and the standard constuctor will pass a null directory to the persistant manager initialization
     * v If a different directory is to be used, the user will call the second constructor with the directory as the parameter, and the second constructur will pass the directory to the persistant manager as a non-null directory
     * v Regardless, pass the newly created PersistanceManager to the BTree
     * v Add constructor that accepts File baseDir as an argument, and it must pass the that baseDir to the DocumentPersistenceManager constructor.
     */

     /**
         *
         * In the previous stages we needed to undo when there was a put, and/or delete
         * This was because, we were putting/deleting/(doing nothing) something to the hashmap/heap/trie
         * Now, we should really start again:
         * These are the structures that can get affected with a standard 
            A. put:
                1. It's possible that there are some document that are going to get moved out. Make a set, that will hold them. 
                2. Clear the space (Meaning, If it is replacing a document):
                    1. You can't stam delete the old doc, because that'll add a command, and we want to add the command here
                    2. If it is in memory:
                        a. Remove from the trie/heap/BTree completely
                        b. Memory does not need to be checked
                        c. No documents were affected
                    3. If it was not in memoruy
                        a. Remove from the trie/BTree completely/not the heap
                        b. Memory does not need to be checked
                        c. No other documents were affected
                    4. Add the document to the list of document that need to added in an undo
                3. Put the document
                    a. Update its time
                    b. It is added to the heap/trie/in memory of the BTree
                    c. Then memory is checked
                    d. Some documents are removed from heap/not from trie/on disk of BTree
                    e. UNDO: 
                        a. remove the document from the heap/trie/BTree
                        b. update times of the removed documents
                        c. add removed documents to the heap/not the trie/on memory of the BTree
                        d. memory needs to be checked
            B. get:
                1. A search is made in the btree
                2. If it was not in memory
                    a. It is added to the heap/not the trie/the memory of the BTree
                    b. Memory is checked
                    c. Some document are sent to memory 
                3. If it was in memory
                    a. It is not added to the heap/not to the trie/not to the memory of the BTree
                    b. Memory need not be checked
                    c. There are no documents that need to be sent to memory
                4. Return the document (even if it was subsequently returned to memory)
            C. search:
                1. A search is made in the Trie
                2. A series of uris come back
                3. Get the documents associated with the results
                4. Some will have been in memory, others not
                5. For those that were already in memory
                    a. It is not added to the heap/not to the trie/not to the memory of the BTree
                    b. Memory need not be checked
                    c. There are no documents that need to be sent to memory
                6. For those that were not in memory
                    a. They are added to the heap/not to the trie/to the memory of the BTree
                    b. Memory needs to be checked
                    c. Some documents are sent to memory
                7. Return all the documents (even if some were returned to memory)
            D. deleteDocument (single):
                1. If it is in memory:
                    a. Remove from the trie/heap/BTree completely
                    b. Memory does not need to be checked
                    c. No documents were affected
                2. If it was not in memoruy
                    a. Remove from the trie/BTree completely/not the heap
                    b. Memory does not need to be checked
                    c. No other documents werw affected
                3. UNDO:
                    a. You need to update its time
                    b. You need to add it to the trie/heap/BTree in memory
                    c. You don't need to move back any, none were affected
                    d. memory needs to be checkec
            E. deleteDocuments
                1. A search is made in the Trie
                2. A series of uris come back
                3. Get the documents associated with the results
                4. Some will have been in memory, others not
                5. For those that were already in memory
                    a. Remove them from trie/heap/BTree completely
                    b. No documents were moved
                    c. Memory does not need to be checked
                6. For those that were not in memory
                    a. Remove them from tire/not heap/BTree completely
                    b. No documents were moved
                    c. Memory was only helped, not hurt
                UNDO:
                    a. Put the documents back in the heap/BTree/Trie
                    b. You don't need to move back any documents, none were affected
                    c. memory needs to be checked


         * 
         */

    public DocumentStoreImpl(){

        this.commandStack = new StackImpl<Undoable>();
        this.documentTrie = new TrieImpl<URI>();
        this.documentMemoryHeap = new MinHeapImpl<DocRep>();
        this.urisInHeap = new HashSet<URI>();
        this.pm = new DocumentPersistenceManager(null);
        
        this.storageBTree = new BTreeImpl<URI, Document>();
        this.storageBTree.setPersistenceManager(pm);

        this.maxDocCountSet = false;
        this.maxDocByteSet = false;

        maxDocumentCount = 0;
        maxDocumentBytes = 0;
        documentsInHeap = 0;
        documentBytesInHeap = 0;

    }

    public DocumentStoreImpl(File directory){

        this.commandStack = new StackImpl<Undoable>();
        this.documentTrie = new TrieImpl<URI>();
        this.documentMemoryHeap = new MinHeapImpl<DocRep>();
        this.urisInHeap = new HashSet<URI>();
        this.pm = new DocumentPersistenceManager(directory);

        this.storageBTree = new BTreeImpl<URI, Document>();
        this.storageBTree.setPersistenceManager(pm);

        this.maxDocCountSet = false;
        this.maxDocByteSet = false;
        maxDocumentCount = 0;
        maxDocumentBytes = 0;
        documentsInHeap = 0;
        documentBytesInHeap = 0;

    } 

    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException{
        
    /**
    A. put:
        1. If it was in memory:
            a. I'm not sure. Either do nothing, or update time etc.
        2. If it was not in memory:
            a. It is added to the heap/trie/in memory of the BTree
            b. Then memory is checked
            c. Some documents are removed from heap/not from trie/on disk of BTree
        5. UNDO: 
            a. remove the document from the heap/trie/BTree
            b. add removed documents to the heap/not the trie/on memory of the BTree
            c. memory needs to be checked
    */


        if (uri == null || format == null || uri.toString().equals("")){
            throw new IllegalArgumentException();
        }

        /**
         * There are two cases:
         * 1. To delete a document that already exists.
         * 2. Create a new document, and put it.
         */

        //1. Deleting a document
        if (input == null){
            DocumentImpl documentToDelete = (DocumentImpl)storageBTree.get(uri);//if the document exists, then it will equal the document. If it does not, then it will equal null
            if (deleteDocument(uri)){
                //if the document exists, then it will be true, and deleted. if it did not exist, then the return will be false and nothing will be deleted
                //All the delete steps, including the undo, is taken care of in the delete method. It will return true if succesful
                return documentToDelete.hashCode();
            } else {
                //stack was altered in the delete method
                return 0;
            }
        }

        //2. Create a new document, and put it

        byte[] contentByteArray = input.readAllBytes();
        DocumentImpl newDoc =  null;
        if (format == DocumentFormat.TXT){
            String contentString = new String(contentByteArray);
            newDoc = new DocumentImpl(uri, contentString);
        } else if (format == DocumentFormat.BINARY){
            newDoc = new DocumentImpl(uri, contentByteArray);
        }

        

        /** 
         * A. put:
                1. It's possible that there are some document that are going to get moved out. Make a set, that will hold them. 
                2. Clear the space (Meaning, If it is replacing a document):
                    1. You can't stam delete the old doc, because that'll add a command, and we want to add the command here
                    2. If it is in memory:
                        a. Remove from the trie/heap/BTree completely
                        b. Memory does not need to be checked
                        c. No documents were affected
                    3. If it was not in memoruy
                        a. Remove from the trie/BTree completely/not the heap
                        b. Memory does not need to be checked
                        c. No other documents were affected
                3. Put the document
                    a. Update its time
                    b. It is added to the heap/trie/in memory of the BTree
                    c. Then memory is checked
                    d. Some documents are removed from heap/not from trie/on disk of BTree
                4. If it replaced a document:
                    a. return the haschode
                    b. UNDO: 
                        a. remove the document from the heap/trie/BTree
                        b. update time of the replaced document
                        c. add it to the heap/trie/btree 
                        b. update times of the removed documents
                        c. add removed documents to the heap/not the trie/on memory of the BTree
                        d. memory needs to be checked
                If it did not replace a document:
                    a. return 0
                    b. UNDO: 
                        a. remove the document from the heap/trie/BTree
                        b. update times of the removed documents
                        c. add removed documents to the heap/not the trie/on memory of the BTree
                        d. memory needs to be checked
         * 
        */

        HashSet<URI> replacedURIs = new HashSet<URI>();

        DocumentImpl previousDocument = (DocumentImpl)storageBTree.put(uri, newDoc);// stores a uri/doc, and save the previous document in previosDocument (if there was one)
        
        if (previousDocument != null){
            removeFromTrie(previousDocument);
            if (urisInHeap.contains(uri)){
                updateHeapRemove(previousDocument);
            }
        } 
        newDoc.setLastUseTime(System.nanoTime());
        replacedURIs.addAll(updateHeapAdd(uri, newDoc, System.nanoTime()));
        addToTrie(newDoc);
        if (previousDocument!= null){
            addGenericCommandPutRemove(newDoc, previousDocument, replacedURIs);
            return previousDocument.hashCode();
        } else {
            addGenericCommandRemove(newDoc, replacedURIs);
            return 0;
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    public Document getDocument(URI uri){
        
        /**
        System.out.println();
        System.out.println("***CLASS: DOCSTORE***");
        System.out.println("***METHOD: GETDOC***");
        */

        if (uri == null){
            throw new IllegalArgumentException("PARAMETER PASSED IS NULL");
        }
            
        /**
         * HEAP COMMENTS
         * 
         * If: This document was put into the BTree, but was deleted
         * Else if: This document was not put into the BTree
         * Return: Null
         * 
         * Else: This document was put, and was not deleted, and therefore retrieved 
         * Check if it is in the heap
         * Update its time
         * If: Was in the heap
            * updateHeapRetrieve
         * Else If: Was not in the heap
            * Update Heap retrieve:
            * 1. Add to heap and heap set
            * 2. Update memory related instance variables
            * 3. move documents to disk if needed
         */

         /**
        B. get:
        1. A search is made in the btree
        2. If it was not in memory
            a. It is added to the heap/not the trie/the memory of the BTree
            b. Memory is checked
            c. Some document are sent to memory 
        3. If it was in memory
            a. It is not added to the heap/not to the trie/not to the memory of the BTree
            b. Memory need not be checked
            c. There are no documents that need to be sent to memory
        4. Return the document (even if it was subsequently returned to memory)
        */
        

        DocumentImpl docToReturn = (DocumentImpl)storageBTree.get(uri);
        if (docToReturn!= null){//This is a file that was in our system
            docToReturn.setLastUseTime(System.nanoTime());
            if (urisInHeap.contains(uri)){//was already in memory | Do not change heap or memory contraints
                //System.out.println("It thinks the document is in memory");
                updateHeapRetrieve(uri);
            } else {
                updateHeapAdd(uri, docToReturn, System.nanoTime());
            }
        } 
        
        return docToReturn;
    }

    /**
     * The Delete methods should make sure of three things:
     * 1. To delete the URI from the HashTable
     * 2. To appropriatly effect the Trie
     * 3. Add a command to the stack
     * 
     * There is another method that deletes from the HashTable: deleteFromHashTable
     * This method must:
     * 1. Call the deleteFromHashTable method
     * 2. Effect the Trie itself
     * 3. Add a command to the Stack
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri){
        
        /**
         * D. deleteDocument (single):
            1. If it is in memory:
                a. Remove from the trie/heap/BTree completely
                b. Memory does not need to be checked
                c. No documents were affected
            2. If it was not in memoruy
                a. Remove from the trie/BTree completely/not the heap
                b. Memory does not need to be checked
                c. No other documents were affected
            3. UNDO:
                a. You need to update its time
                b. You need to add it to the trie/heap/BTree in memory
                c. You don't need to move back any, none were affected
                d. memory needs to be checkec
         */

        if (uri == null){
            throw new IllegalArgumentException("PARAMETER PASSED IS NULL");
        }
            
        /**
        System.out.println("About to null out: " + uri + " becuase it was explicitly deleted");
        System.out.println("Before...");
        printHeap();
        printUrisSet();
        */
        DocumentImpl docToDelete = (DocumentImpl)storageBTree.get(uri);

        if (docToDelete != null){
            removeFromTrie(docToDelete);
            if (urisInHeap.contains(uri)){
                updateHeapRemove(docToDelete);
                storageBTree.put(uri, null);
            }
            addGenericCommandPut(docToDelete);
            return true;
        } else {
            addGenericCommandEmpty(uri);
            return false;
        }   

    }
    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException{

        /**
         * Memory is taken care of when the item is undone
         * We need to only worry about the time
         * If the undo resulted in a put, we should update the time
         *      Then we need to reheapafy. You can either call this yourself, or call the updatememorybecauseofget
         * If it resulted in a delete, we do not need to
         * 
         */

        try {
            Undoable commandToUndo = commandStack.pop();
            commandToUndo.undo();

            if (commandToUndo instanceof GenericCommand){
                URI uri = (URI)((GenericCommand)commandToUndo).getTarget();
                this.getDocument(uri);
                //time and memory altercation was taken care of in the getMethod
            } else {
                long time = System.nanoTime();
                for (Object nextUri : (CommandSet)commandToUndo){
                    DocumentImpl doc = (DocumentImpl)this.getDocument((URI)nextUri);
                    if(doc!= null){
                        //this means that the document is in the store
                        //after calling the get method, the time of the document was altered, and the readjusted
                        //but we need all these document to have the same time
                        doc.setLastUseTime(time);
                        updateHeapRetrieve(doc.getKey());
                    }

                }
            }

        } catch (NullPointerException e){
            throw new IllegalStateException ("NOTHING TO DO | COMMAND STACK IS EMPTY");
        }

    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI uri) throws IllegalStateException{
        if (uri == null){
            throw new IllegalArgumentException("URI PASSED AS PARAMETER IS NULL");
        }

        /**
        System.out.println("\nUndo is called");
        System.out.println("The command stack is this tall: " + commandStack.size());
        */
        
        StackImpl <Undoable> tempStack = new StackImpl<Undoable>();
        
        try {

            //System.out.println("the stack is " + commandStack.size() + " command(s) tall");

            boolean found = false;

            while (!found){

                //System.out.println("\nA rotation through the undo loop");
                if (commandStack.peek() instanceof GenericCommand){//It is a generic command
                    //System.out.println("Next command is a generic command");
                    GenericCommand nextCommand = (GenericCommand) commandStack.peek();
                    if (nextCommand.getTarget().equals(uri)){
                        /** 
                        System.out.println();
                        System.out.println("**********************************************");
                        System.out.println("Found it");
                        */

                        nextCommand.undo();
                        //if it was meant to, it was reinstated into the store in the undo method
                        //but we need to update time
                        //System.out.println("Succesfully undone");
                        this.getDocument(uri);
                        //if the document exists, then it was reinstated, and its time updated, and the heap adjusted
                        commandStack.pop();
                        found = true;
                    } else {
                        commandStack.pop();
                        tempStack.push(nextCommand);
                    }
                } else {
                    //System.out.println("Next command is a command set");
                    CommandSet<URI> nextCommand = (CommandSet) commandStack.peek();
                    if (nextCommand.containsTarget(uri)){
                        //System.out.println("This command set contains the command with a URI: " + uri);
                        nextCommand.undo(uri);
                        //if it was meant to, it was reinstated into the store in the undo method
                        //but we need to update time
                        this.getDocument(uri);
                        //if the document exists, then it was reinstated, and its time updated, and the heap adjusted
                        if (nextCommand.size() == 0){
                            commandStack.pop();
                        }
                        found = true;
                    } else {
                        commandStack.pop();
                        tempStack.push(nextCommand);
                    }
                }
            }

            //System.out.println("\nLoop is done");
            //System.out.println("With the conclusion of the undo, the command stack is this tall: " + commandStack.size());

            int tempSize = tempStack.size();
        
            for (int i = 0; i < tempSize; i++){
                commandStack.push(tempStack.pop());
            }

        } catch (NullPointerException e){

            int tempSize = tempStack.size();
        
            for (int i = 0; i < tempSize; i++){
                commandStack.push(tempStack.pop());
            }

            throw new IllegalStateException ("ACTION NOT FOUND");
        }

    }
    
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword){
        /** 
         * search:
            1. A search is made in the Trie
            2. A series of uris come back
            3. Get the documents associated with the results
            4. Some will have been in memory, others not
            5. For those that were already in memory
                a. The heap is reshaped/not to the trie/not to the memory of the BTree
                b. Memory need not be checked
                c. There are no documents that need to be sent to memory
            6. For those that were not in memory
                a. They are added to the heap/not to the trie/to the memory of the BTree
                b. Memory needs to be checked
                c. Some documents are sent to memory
            7. Return all the documents (even if some were returned to memory)
         * 
        */
        
        /** 
        System.out.println();
        System.out.println("Preforming search.");
        */

        keyword = fixText(keyword);
        
        List<URI> returnedList = documentTrie.getAllSorted(keyword, createWordComparator(keyword));
        //System.out.println("Number of URIs that came back: " + returnedList.size());
        List<Document> documentsReturned = new ArrayList<Document>();
        
        long time = System.nanoTime();
        
        for (URI nextURI : returnedList){
            
            /**
            System.out.println();
            System.out.println("Found the URI: " + nextURI + ", after searching for " + keyword);
            */

            DocumentImpl docFound = (DocumentImpl)storageBTree.get(nextURI);
            documentsReturned.add(docFound);
            if (!urisInHeap.contains(nextURI)){
                updateHeapAdd(nextURI, docFound, time);
            } else {
                docFound.setLastUseTime(time);
                updateHeapRetrieve(nextURI);
            }
        }
        
        return documentsReturned;

    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix){

        /** 
         * search:
            1. A search is made in the Trie
            2. A series of uris come back
            3. Get the documents associated with the results
            4. Some will have been in memory, others not
            5. For those that were already in memory
                a. The heap is reshaped/not to the trie/not to the memory of the BTree
                b. Memory need not be checked
                c. There are no documents that need to be sent to memory
            6. For those that were not in memory
                a. They are added to the heap/not to the trie/to the memory of the BTree
                b. Memory needs to be checked
                c. Some documents are sent to memory
            7. Return all the documents (even if some were returned to memory)
         * 
        */

        keywordPrefix = fixText(keywordPrefix);

        List<URI> returnedList = documentTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix));
        List<Document> documentsReturned = new ArrayList<Document>();

        long time = System.nanoTime();

        for (URI nextURI : returnedList){
            DocumentImpl docFound = (DocumentImpl)storageBTree.get(nextURI);
            documentsReturned.add(docFound);
            if (!urisInHeap.contains(nextURI)){
                updateHeapAdd(nextURI, docFound, time);
            } else {
                docFound.setLastUseTime(time);
                updateHeapRetrieve(nextURI);
            }
        }
        
        return documentsReturned;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword){
        
        /**
         * deleteDocuments
                1. A search is made in the Trie
                2. A series of uris come back
                3. Get the documents associated with the results
                4. Some will have been in memory, others not
                5. For those that were already in memory
                    a. Remove them from trie/heap/BTree completely
                    b. No documents were moved
                    c. Memory does not need to be checked
                6. For those that were not in memory
                    a. Remove them from trie/not heap/BTree completely
                    b. No documents were moved
                    c. Memory was only helped, not hurt
                UNDO:
                    a. Put the documents back in the heap/BTree/Trie
                    b. You don't need to move back any documents, none were affected
                    c. memory needs to be checked
         */

        keyword = fixText(keyword);


        /**
         * get all documents with the keyword
         * They will all still exist in their place in the trie
         * Delete them 
         * This delete will delete the documents from the HashSet as well as the Trie
         * Deleting from the Trie will analyze all the words, and remove them from all the corresponding places in the trie
         **/
        Set<URI> releventURIs = new HashSet(documentTrie.getAllSorted(keyword, createWordComparator(keyword)));
        Set<DocumentImpl> relevantDocuments = new HashSet<DocumentImpl>();

        for (URI nextURI : releventURIs){
            Document retrievedDoc = storageBTree.get(nextURI);
            relevantDocuments.add((DocumentImpl)retrievedDoc);

            if (urisInHeap.contains(nextURI)){
                removeFromTrie((DocumentImpl)retrievedDoc);
                updateHeapRemove((DocumentImpl)retrievedDoc);
                storageBTree.put(nextURI, null);
            } else {
                removeFromTrie((DocumentImpl)retrievedDoc);
                storageBTree.put(nextURI, null);
            }
        }

        Set<GenericCommand> allGenericCommands = new HashSet<GenericCommand>();
        for (DocumentImpl nextDoc : relevantDocuments){
            allGenericCommands.add(makeGenericCommandPut(nextDoc));
        }
        addCommandSet(allGenericCommands);    

        return releventURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        
        /**
         * deleteDocuments
                1. A search is made in the Trie
                2. A series of uris come back
                3. Get the documents associated with the results
                4. Some will have been in memory, others not
                5. For those that were already in memory
                    a. Remove them from trie/heap/BTree completely
                    b. No documents were moved
                    c. Memory does not need to be checked
                6. For those that were not in memory
                    a. Remove them from trie/not heap/BTree completely
                    b. No documents were moved
                    c. Memory was only helped, not hurt
                UNDO:
                    a. Put the documents back in the heap/BTree/Trie
                    b. You don't need to move back any documents, none were affected
                    c. memory needs to be checked
         */

        keywordPrefix = fixText(keywordPrefix);

        Set<URI> releventURIs = new HashSet(documentTrie.getAllWithPrefixSorted(keywordPrefix, createPrefixComparator(keywordPrefix)));
        Set<DocumentImpl> relevantDocuments = new HashSet<DocumentImpl>();

        for (URI nextURI : releventURIs){
            Document retrievedDoc = storageBTree.get(nextURI);
            relevantDocuments.add((DocumentImpl)retrievedDoc);

            if (urisInHeap.contains(nextURI)){
                removeFromTrie((DocumentImpl)retrievedDoc);
                updateHeapRemove((DocumentImpl)retrievedDoc);
                storageBTree.put(nextURI, null);
            } else {
                removeFromTrie((DocumentImpl)retrievedDoc);
                storageBTree.put(nextURI, null);
            }
        }

        Set<GenericCommand> allGenericCommands = new HashSet<GenericCommand>();
        for (DocumentImpl nextDoc : relevantDocuments){
            allGenericCommands.add(makeGenericCommandPut(nextDoc));
        }
        addCommandSet(allGenericCommands);    

        return releventURIs;

    }

    /**
     * set maximum number of documents that may be stored
     * @param limit
     */
    public void setMaxDocumentCount(int limit){
        maxDocCountSet = true;
        this.maxDocumentCount = limit;
        moveEnoughToDisk();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     * @param limit
     */
    public void setMaxDocumentBytes(int limit){
        maxDocByteSet = true;
        this.maxDocumentBytes = limit;
        moveEnoughToDisk();
    }


    /**
     * Commands manage memory, but not time
     * Manage memories, manage memory, but not time
     * Every time you make a call to a managememory, you should be taking care of the time
     * If you call undo, they will call the managememory, but you need to take care of the time
     */



    private void addToTrie(DocumentImpl docToAdd){
        Set<String> wordsInDocument = docToAdd.getWords();

        for (String nextWord : wordsInDocument){
            documentTrie.put(nextWord, docToAdd.getKey());
        }
    }

    private void removeFromTrie (DocumentImpl deletedDocument){
        
        for (String nextWord: deletedDocument.getWords()){
            documentTrie.delete(nextWord, deletedDocument.getKey());
        }
        
    }

    //If any of these are putting

    private void addGenericCommandPut (DocumentImpl docToPut){
        
        GenericCommand newCommand = makeGenericCommandPut(docToPut);
        commandStack.push(newCommand);
    }

    private void addGenericCommandEmpty(URI specificURI){

        //System.out.println("Adding a generic command");

        GenericCommand newCommand = makeGenericCommandEmpty(specificURI);
        commandStack.push(newCommand);
    }

    private void addGenericCommandRemove (DocumentImpl docToRemove, HashSet<URI>swappedDocsURI){
        
        //System.out.println("Adding a remove command\n");

        GenericCommand<URI> newCommand = makeGenericCommandRemove(docToRemove, swappedDocsURI);
        commandStack.push(newCommand);

    }

    private void addGenericCommandPutRemove (DocumentImpl docToRemove, DocumentImpl docToPut, HashSet<URI> swappedDocsURI){
        

        GenericCommand newCommand = makeGenericCommandRemovePut(docToRemove, docToPut, swappedDocsURI);
        commandStack.push(newCommand);
    }

    private void addCommandSet (Set<GenericCommand> setOfGenericCommands){

        CommandSet newCommandSet = makeCommandSet(setOfGenericCommands);
        commandStack.push(newCommandSet);
    }

    private GenericCommand makeGenericCommandPut (DocumentImpl docToPut){

        /**
         * D. deleteDocument (single):
            1. If it is in memory:
                a. Remove from the trie/heap/BTree completely
                b. Memory does not need to be checked
                c. No documents were affected
            2. If it was not in memoruy
                a. Remove from the trie/BTree completely/not the heap
                b. Memory does not need to be checked
                c. No other documents were affected
            3. UNDO:
                a. You need to update its time
                b. You need to add it to the trie/heap/BTree in memory
                c. You don't need to move back any, none were affected
                d. memory needs to be checkec
         */

        URI specificURI = docToPut.getKey();
        
        Function<URI, Boolean> function = (genericURI) -> {
            
            /**
            System.out.println("************************************************************************");
            System.out.println("Going to put back in: " + docToPut.getKey());
            */

            storageBTree.put(genericURI, docToPut);
            addToTrie(docToPut);
            updateHeapAdd(genericURI, docToPut, System.nanoTime());
            return true;};
    
        return new GenericCommand(specificURI, function);
    }

    private GenericCommand makeGenericCommandEmpty(URI specificURI){
        Function<URI, Boolean> function = (genericURI) -> {return true;};
        return new GenericCommand(specificURI, function);
    }

    private GenericCommand makeGenericCommandRemove (DocumentImpl docToRemove, HashSet<URI>swappedDocsURI){
        
        /**
        If it did not replace a document:
            a. return 0
            b. UNDO: 
                a. remove the document from the heap/trie/BTree
                b. update times of the removed documents
                c. add removed documents to the heap/not the trie/on memory of the BTree
                d. memory needs to be checked
        */

        URI specificURI = docToRemove.getKey();
        
        Function<URI, Boolean> function = (genericURI) -> {
            removeFromTrie((DocumentImpl)storageBTree.get(genericURI));
            updateHeapRemove((DocumentImpl)storageBTree.get(genericURI));
            storageBTree.put(genericURI, null);

            long time = System.nanoTime();

            for (URI nextURI : swappedDocsURI){
                
                DocumentImpl doc = (DocumentImpl)storageBTree.get(nextURI);
                updateHeapAdd(nextURI, doc, time);
            }

            return true;
        };
        return new GenericCommand<URI>(specificURI, function);

    }

    private GenericCommand makeGenericCommandRemovePut (DocumentImpl docToRemove, DocumentImpl docToPut,  HashSet<URI> swappedDocsURI){
        
        /**
         * 
         * If it replaced a document:
            a. return the haschode
            b. UNDO: 
                a. remove the document from the heap/trie/BTree
                b. update time of the replaced document
                c. add it to the heap/trie/btree 
                b. update times of the removed documents
                c. add removed documents to the heap/not the trie/on memory of the BTree
                d. memory needs to be checked
         * 
         */
        
        URI specificURI = docToRemove.getKey();
        
        Function<URI, Boolean> function = genericURI -> {
            removeFromTrie((DocumentImpl)storageBTree.get(genericURI));
            updateHeapRemove((DocumentImpl)storageBTree.get(genericURI));
            storageBTree.put(genericURI, null);

            long time = System.nanoTime();
            docToPut.setLastUseTime(time);

            storageBTree.put(docToPut.getKey(), docToPut);
            addToTrie(docToPut);
            updateHeapAdd(docToPut.getKey(), docToPut, time);

            for (URI nextURI : swappedDocsURI){
                DocumentImpl doc = (DocumentImpl)storageBTree.get(nextURI);
                updateHeapAdd(nextURI, doc, time);
            }

            return true;};
        return new GenericCommand(specificURI, function);
    }

    private CommandSet makeCommandSet (Set<GenericCommand> setOfGenericCommands){
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (GenericCommand nextGenericCommand : setOfGenericCommands){
            commandSet.addCommand(nextGenericCommand);
        }
        return commandSet;
    }

    //makes sure that only one word is passed
    private String fixText(String word){
        if (word.contains(" ")){
            throw new IllegalArgumentException("This method only takes in one word or prefix.\nYou passed in a String with a space");
        } else {
            return parse(word.toLowerCase());
        }
    }

    private String parse (String txt){
        txt = txt.toLowerCase();
        return txt.replaceAll("[_\\W]+", "");
    }

    private Comparator<URI> createWordComparator (String key){

        Comparator<URI> compareTwoDocs = (URI uri1, URI uri2) -> {
            
            if (storageBTree.get(uri1).wordCount(key) < storageBTree.get(uri2).wordCount(key)){
                return 1;
            } else if (storageBTree.get(uri2).wordCount(key) < storageBTree.get(uri1).wordCount(key)){
                return -1;
            } else {
                return 0;
            }
        };

        return compareTwoDocs;

    }

    private Comparator<URI> createPrefixComparator (String prefix){

        Comparator<URI> compareTwoDocs = (URI uri1, URI uri2) -> {
            
            //System.out.println("\nComparing " + doc1.getKey() + " and " + doc2.getKey());

            int doc1PrefixCount = 0;
            int doc2PrefixCount = 0;

            for (String nextWord : storageBTree.get(uri1).getWords()){
                if (nextWord.startsWith(prefix)){
                    doc1PrefixCount++;
                    //System.out.println("there was a prefix found in " + doc1.getKey());
                    //System.out.println("the number of words with a prefix in doc1 is now: " + doc1PrefixCount);
                }
            }
            for (String nextWord : storageBTree.get(uri2).getWords()){
                if (nextWord.startsWith(prefix)){
                    doc2PrefixCount++;
                    //System.out.println("there was a prefix found in " + doc2.getKey());
                    //System.out.println("the number of words with a prefix in doc2 is now: " + doc2PrefixCount);
                }
            }

            if (doc1PrefixCount < doc2PrefixCount){
                return 1;
            } else if (doc2PrefixCount < doc1PrefixCount){
                return -1;
            } else {
                return 0;
            }
        };

        return compareTwoDocs;

    }

    private HashSet<URI> moveEnoughToDisk (){
        /**
         * 1. Check memory status
         * 2. If there is a contradiction, move documents to disk
         *  a. Get min
         *  b. Move min to disk
         *      1. Remove from heap (This was done with the get min) (Both the heap and the heapset)
                2. Update memory variables
         *      3. Send to disk
                4. Add the URI to the HashSet
         *  c. Continue a - b until there is enough memory
         * 3. If there is not, return
         */

         HashSet<URI> hashSetToReturn = new HashSet<URI>();

        if (!maxDocByteSet && !maxDocCountSet){
            return hashSetToReturn;
        } else {
            while (((maxDocByteSet)&&(documentBytesInHeap > maxDocumentBytes)) || ((maxDocCountSet)&&(documentsInHeap > maxDocumentCount))){            

                /**
                System.out.println();
                System.out.println("Moving something to memory");
                System.out.println("Before:");
                printUrisSet();
                printHeap();
                */
                

                DocRep removedDocRep = documentMemoryHeap.remove();
                urisInHeap.remove(removedDocRep.getUri());

                /**
                System.out.println();
                System.out.println("Just removed: " + removedDocRep.getUri() + " both from heap and the urisset. Now check.");
                printUrisSet();
                printHeap();
                */
                


                DocumentImpl removedDoc = (DocumentImpl)storageBTree.get(removedDocRep.getUri());

                documentsInHeap--;
                documentBytesInHeap -= getMemoryLength(removedDoc); 

                hashSetToReturn.add(removedDoc.getKey());
                try {
                    storageBTree.moveToDisk(removedDocRep.getUri());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            return hashSetToReturn;

        }
    }

    private int getMemoryLength(DocumentImpl doc){

        /**
        System.out.println();
        System.out.println("***CLASS: DOCSTORE***");
        System.out.println("***METHOD: GETMEMLENGTH***");
        */

        String docText = doc.getDocumentTxt();
        byte[] byteArray = doc.getDocumentBinaryData();

        if (docText != null){
            return docText.getBytes().length;
        } else {
            return byteArray.length;
        }

    }

    /**
     * The following three methods:
     * 0. DO NOT UPDATE TIME. Update time yourself for (put/retrieve)
     * 1. Updates content of the heap (the heap and the heapset) (add/remove)
     * 2. Changes the structure of the heap (but does not affect the time itself) (retrieve/remove)
     * 3. Change the appropriate memory related instance variables (add/remove)
     * 4. Removes documents if needed (put)
     */

    private HashSet<URI> updateHeapAdd(URI uri, DocumentImpl doc, long time){

        /**
        System.out.println();
        System.out.println("Updating memory for add");
        System.out.println("Before");
        printHeap();
        printUrisSet();
        */
        

        documentsInHeap++;
        documentBytesInHeap += getMemoryLength(doc); 
        
        doc.setLastUseTime(time);
        DocRep docRep = new DocRep(uri);
        documentMemoryHeap.insert(docRep);
        //System.out.println("here");
        urisInHeap.add(uri);
        //no need to reheapify, because you just added the document!
        
        
        /**
        System.out.println();
        System.out.println("After, but before moved to disk");
        printHeap();
        printUrisSet();
        */
        

        return moveEnoughToDisk();
        
    }

    private void updateHeapRetrieve (URI uri){
        
        DocRep docRep = new DocRep(uri);

        documentMemoryHeap.reHeapify(docRep);
    }

    private void updateHeapRemove (DocumentImpl deletedDoc){
        
        //printHeap();

        DocRep docToBeat =  documentMemoryHeap.remove();

        if (docToBeat.getUri() != deletedDoc.getKey()){
            long timeToBeat = storageBTree.get(docToBeat.getUri()).getLastUseTime();

        
            documentMemoryHeap.insert(docToBeat);

            deletedDoc.setLastUseTime(timeToBeat - 1);
            DocRep docRep = new DocRep (deletedDoc.getKey());
            documentMemoryHeap.reHeapify(docRep);

            documentMemoryHeap.remove();
        }             
        
        urisInHeap.remove(deletedDoc.getKey());

        documentsInHeap--;
        documentBytesInHeap -= getMemoryLength(deletedDoc); 

    }

    class DocRep implements Comparable<DocRep> { 
        URI uri;

        DocRep(URI uri){
            this.uri = uri;
        }

        private URI getUri(){
            return this.uri;
        }

        @Override
        public int compareTo(DocRep toCompare) {

            DocumentImpl firstDoc = (DocumentImpl)storageBTree.get(this.uri);
            DocumentImpl secDoc =  (DocumentImpl)storageBTree.get(toCompare.getUri());

            /**
            System.out.println();
            System.out.println("Within docrep");
            printHeap();
            printUrisSet();
            System.out.println();
            System.out.println(this.uri + " is being compared to: " + toCompare.getUri());
            */

            if (firstDoc == null){
                //System.out.println("First doc is null");
            }
            if (secDoc == null) {
                //System.out.println("Second doc is null");
            }


            return firstDoc.compareTo(secDoc);
        }

        @Override
        public boolean equals(Object o){
            if (this == o) {
                return true;
            } else if (!(o instanceof DocRep)){
                return false;
            } 
            
            DocRep docRepToCompare = (DocRep)o;

            /**
            System.out.println("Comparing Two URIs:");
            System.out.println("The First: " + this.uri);
            System.out.println("The Second: " + docRepToCompare.getUri());
            */
            

            if (this.uri.equals(docRepToCompare.getUri())){
                return true;
            } else {
                return false;
            }

        }
    }

    /** 
    private void printHeap (){

        System.out.println("HEAP:");

        int i = 0;
        for (DocRep nextDoc : documentMemoryHeap.getArray()){
            if (nextDoc == null){
                System.out.println("" + i + ". null");
            } else {
                System.out.println("" + i + ". " + nextDoc.getUri() + " (" + nextDoc +")");
            }
            i++;
        }
    }
    */

    /** 
    private void printUrisSet(){

        System.out.println("URISET:");

        for (URI nextURI: urisInHeap){
            System.out.println("" + nextURI);
        }
    }
    */
    
}

        

