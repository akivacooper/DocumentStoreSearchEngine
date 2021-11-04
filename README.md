
# DocumentStoreSearchEngine

_Preface:_

A program capable of storing and searching documents both in memory and on disk with GSON serialization

Implements and intertwines a range of data structures—including a trie to implement search within documents, a BTree which takes document keys (of type URI) as parameters returning the associated value, and a heap to keep track of which documents are most recently accessed and which to serialize to disk—to successfully implement the search engine; data structures are mostly created from scratch

**Section 1: An In-Memory Document Store (BTree)**
========================

_Relevant Classes:_

BTree Interface (edu.yu.cs.com1320.project.BTree)\
Document Interface (edu.yu.cs.com1320.project.stage5.Document)\
DocuemtnStore Interface (edu.yu.cs.com1320.project.stage5.DocumentStore)\
DocumentStoreImpl Implementation (edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl)\

_General description of Section 1:_
1. This section describes the program&#39;s storage mechanism. The program supports &quot;get&quot; and &quot;put&quot; of data. Documents are stored in memory and on disk using a BTree and can only be retrieved using the key (URI) with which they are stored. Documents can be plain text, a.k.a. a String, or binary data, e.g. images

_Description of logic in Section 1:_

1. BTree implementation
    1. The primary storage structure for documents is a BTree.
    2. An entry in the BTree can have 3 different things as its Value:
        1. If the entry is in an internal BTree node, the value must be a link to another node in the BTree
        2. If the entry is in a leaf/external BTree node, the value can be either:
            1. a pointer to a Document object in memory or to
            2. a reference to where the document is stored on disk (if and only if it has been written out to disk)
2. Document interface
    1. A Document is made up of a unique identifier (java.net.URI) and the data that comprises the content of the document, either a java.lang.String for text or a byte[] for binary data
    2. DocumentImpl overrides the default equals and hashCode methods. Two documents are considered equal if they have the same hashCode
    3. DocumentImpl provides the following two constructors, which throw an java.lang.IllegalArgumentException if either argument is null or empty/blank:
        1. public DocumentImpl(URI uri, String txt)
        2. public DocumentImpl(URI uri, byte[] binaryData)
3. DocumentStoreImpl
    1. Implements the DocumentStore interface, which specifies an API to:
        1. put a Document in the store
        2. get a Document from the store
        3. delete a Document
        4. DocumentStore uses an instance of BTreeImpl to store documents in memory and disk
    2. If a user calls deleteDocument, or putDocument with null as the value, the program completely removes any/all vestiges of the Document with the given URI from the BTree, including any objects created to house it
    3. The code will receive documents as an InputStream and the document&#39;s key as an instance of URI. When a document is added to the DocumentStore, the code does the following:
        1. Reads the entire contents of the document from the InputStream into a byte[]
        2. Creates an instance of DocumentImpl with the URI and the String or byte[]that was passed to you.
        3. Inserts the Document object into the BTree with URI as the key and the Document object as the value
        4. Returns the hashCode of the previous document that was stored in the BTree at that URI, or zero if there was none

**Section 2: Undo Support to the Document Store Using a Stack**
========================

_Relevant Classes:_
Document Interface (edu.yu.cs.com1320.project.stage5.Document)\
Stack Interface (edu.yu.cs.com1320.project.Stack)\
Stack Implementation (edu.yu.cs.com1320.project.impl.StackImpl)\

_General description of Section 2:_
1. This section describes support for two different types of undos using lamda expressions:
    1. undo the last action, no matter what Document it was done to
    2. undo the last action on a specific Document

_Description of logic in Section 2:_
1. DocumentStore
    1. DocumentStore supports undo via a Undoable Stack
    2. Every call to DocumentStore.putDocument() and DocumentStore.deleteDocument() results in the adding of a new instance of a Undoable() to a single StackImpl which implements Stack; this stack serves as an Undoable Stack
    3. If a user calls DocumentStore.undo(), then the DocumentStore undoes the last Undoable on the Stack
    4. If a user calls DocumentStore.undo(URI), then the DocumentStore undoes the last Undoable on the Stack that was done on the Document whose key is the given URI, without having any permanent effects on any Undoables that are on top of it in the Undoable Stack
    5. Undo is achieved by DocumentStore calling the Undoable.undo() method on the Undoble that represents the action to be undone. DocumentStore does not implement the actual undo logic itself, although it does manage the Undoable Stack and determines which undo to call on which Undoable
2. Undo logic
    1. There are two types of undo&#39;s when undoing a DocumentStore.putDocument():
        1. The call to putDocument added a brand new Document to the DocumentStore
        2. The call to putDocument resulted in overwriting an existing Document with the same URI in the DocumentStore
    2. To undo a call to DocumentStore.deleteDocument(), whatever was deleted is put back into the DocumentStore exactly as it was before; this is done without adding new Undoables to the Undoable Stack; meaning, the undo itself is not &quot;recorded&quot; as an Undoable on the Undoable Stack, rather it simply causes the undoing of some pre-existing Undoable. Once the undo process is completely done, there is no record at all of the fact that an undo took place
3. Functional implementations for undo
    1. As stated above, every put and delete done in the DocumentStore results in the adding of a new Undoable onto the Undoable Stack
    2. Undo is defined as lambda functions that are passed as arguments to the Undoable&#39;s constructor

**Section 3: Keyword Search Using a Trie**
========================

_Relevant Classes:_
Trie Interface (edu.yu.cs.com1302.project.Trie)\
Trie Implementation (edu.yu.cs.com1320.project.impl.TrieImpl)\
DocuemtnStore Interface (edu.yu.cs.com1320.project.stage5.DocumentStore)\
DocumentStoreImpl Implementation (edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl)\

_General description of Section 3:_
1. This section describes key word search capability in the document store. A user can call DocumentStore.search(keyword) to get a list of documents in the DocumentStore that contain the given keyword. The data structure used for searching is a Trie

_Description of logic in Section 3:_
1. A Trie Which Will Be Used for Searching The Document Store.
2. Miscellaneous Points:
    1. Searching and word counting are case insensitive. That means that in both the keyword and the document, &quot;THE&quot;, &quot;the&quot;, &quot;ThE&quot;, &quot;tHe&quot;, etc. are all considered to be the same word
    2. Search results are returned in descending order. That means that the Document in which a word appears the most times is first in the returned list, the Document with the second most matches is second, etc.
    3. TrieImpl uses a java.util.Comparator\&lt;Document\&gt; to sort collections of Documents by how many times a given word appears in them, when implementing Trie.getAllSorted() and any other Trie methods that return a sorted collection
    4. Any search method in TrieImpl or DocumentStoreImpl that returns a collection returns an empty collection, not null, if there are no matches
3. When a Document is added to the DocumentStore
    1. DocumentStore goes through the document and creates a java.util.HashMap that will be stored in the Document object that maps all the words in the Document to the number of times the word appears in the Document
    2. It ignores all characters that are not a letter or a number
    3. This is important for the Document.wordCount() implementation and also for its interactions with the Trie
    4. For each word that appears in the Document, the Document is added to the Value collection at the appropriate Node in the Trie
4. When a Document is deleted from DocumentStore
    1. All references to it within all parts of the Trie are deleted
    2. If the Document being removed is that last one at that node in the Trie, the program deletes it and all ancestors between it and the closest ancestor that has at least one Document in its Value collection
5. Undo
    1. All Undo logic also deals with updating the Trie appropriately
    2. If an Undoable involves a single document, an instance of GenericCommand is created and pushed onto the Undoable Stack. If, however, the Undoable involves multiple documents/URIs, an instance of CommandSet is created to capture the information about the changes to each document. The CommandSet is only removed from the Undoable Stack once the CommandSet has no commands left in it due to undo(uri) being called on the URIs of all the GenericCommands in the CommandSet

**Section 4: Memory Management**\
**Tracking Document Usage via a Heap**\
**Two Tier Storage (RAM and Disk) Using a BTree**
========================

_Relevant Classes:_
BTree Interface (edu.yu.cs.com1320.project.BTree)\n
Document Interface (edu.yu.cs.com1320.project.stage5.Document)\n
DocuemtnStore Interface (edu.yu.cs.com1320.project.stage5.DocumentStore)\n
DocumentStoreImpl (edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl)\n
Trie Interface (edu.yu.cs.com1302.project.Trie)\n
Trie Implementation (edu.yu.cs.com1320.project.impl.TrieImpl)\n
MinHeap Interface (edu.yu.cs.com1320.project.MinHeap)\n
MinHeap Implementation (edu.yu.cs.com1320.project.impl.MinHeapImpl)\n
Document Persistence Manage Implementation (edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager)

_General description of Section 4:_
1. This section describes use a MinHeap to track the usage of Documents in the DocumentStore. Only a fixed number of Documents are allowed in memory at once, and when that limit is reached, adding an additional document results in the least recently used Document being deleted from memory and serialized to disc

_Description of logic in Section 4:_
1. Queue Documents by usage time via a MinHeap
    1. After a Document is used and its lastUsedTime is updated, that Document may now be in the wrong place in the Heap. Therefore MinHeapImpl.reHeapify() is called. The job of reHeapify() is to determine whether the Document whose time was updated should stay where it is, move up in the Heap, or move down in the Heap, and then carry out any move that should occur
    2. Document extends Comparable\&lt;Document\&gt;, and the comparison is made based on the last use time (see next point) of each Document
2. Track Document usage time
    1. The Document interface has the following methods:
        1. long getLastUseTime() and
        2. void setLastUseTime(long timeInNanoSeconds)
    2. Every time a Document is used, its last used time is updated to the relative JVM time, as measured in nanoseconds. A Document is considered to be &quot;used&quot; whenever it is accessed as a result of a call to any part of DocumentStore&#39;s public API. In other words, time is updated if it is &quot;put&quot;, or returned in any form as the result of any &quot;get&quot; or &quot;search&quot; request, or an action on it is undone via any call to either of the DocumentStore.undo() methods
3. Enforce memory limits
    1. The DocumentStore interface has the following methods:
        1. setMaxDocumentCount(int limit). This sets the maximum number of documents that may be stored
        2. setMaxDocumentBytes(int limit). This sets the maximum number of bytes of memory that may be used by all the documents in memory combined
    2. When the program first starts, there are no memory limits. However, the user may call either (or both) of the methods shown above on the DocumentStore to set limits on the storage used by Documents. If both setters have been called by the user, then memory is considered to be full if either limit is reached
    3. For purposes of this program, the memory usage of a Document is defined as the total number of bytes in the Document&#39;s in-memory representation. For text, that&#39;s the length of the array returned by String.getBytes(), and for a binary Document it is the length of the binary data, i.e. the byte[]
    4. When carrying out a &quot;put&quot; or an &quot;undo&quot; will push the DocumentStore above either memory limit, the DocumentStore gets the least recently used Document from the MinHeap, and then it will be written to disk via a call to BTree.moveToDisk(). When a document is moved to disk, the entry in the BTree has a reference to the file on disk as its value instead of a reference to the document in memory. When a Document is written out to disk, it is removed from the MinHeap which is managing memory
    5. No data structure in the DocumentStore other than the BTree has a direct reference to the Document object. Other data structures only have the Document URI, and call BTree.get() whenever they need any piece of information from the Document, e.g. it&#39;s lastUseTime, its byte[], etc.
    6. If BTree.get() is called with a key/URI whose Document has been written to disk, that document is brought back into memory. If bringing it into memory causes memory limits to be exceeded, other documents are written out to disk until the memory limit is conformed with. When a Document is brought back into memory from disk:
        1. its lastUseTime must be set to the current time
        2. its file on disk must be deleted
    7. If BTree.put() is called with a key/URI that already has an entry in the BTree but its Document has been written to disk, i.e. the document on disk is going to be deleted and replaced with a new document. DocumentPersistenceManager.delete() is called within the BTree.put() logic in order to delete the old document from disk
4. Document serialization and deserialization
    1. What to serialize
        1. lastUseTime is not serialized. The following will be serialized/deserialized:
            1. the contents of the document (String or binary)
            2. the URI
            3. the wordcount map
       2. The following are relevant methods in the Document interface:
            1. Map\&lt;String,Integer\&gt; getWordMap(). This returns a copy of the wordcount map so it can be serialized
            2. void setWordMap(Map\&lt;String,Integer\&gt; wordMap). This sets the wordcount map during deserialization
    2. Document (de)serialization
        1. BTreeImpl does not implement (de)serialization itself. When DocumentStoreImpl is initializing itself, it calls BTreeImpl.setPersistenceManager and passes it an instance of which will do all the disk I/O for the BTree. BTreeImpl uses the DocumentPersistenceManager for all disk I/O
        2. By default, the DocumentPersistenceManager will serialize/deserialize to/from the working directory of the application. However, if the caller passes in a non-null baseDir as a constructor argument when creating the DocumentPersistenceManager, then all serialization/deserialization occurs from that directory instead
        3. DocumentPersistenceManager uses instances of com.google.gson.JsonSerializer\&lt;Document\&gt; and com.google.gson.JsonDeserializer\&lt;Document\&gt; to (de)serialize from/to disk
        4. Notice DocumentStoreImpl&#39;s second constructor that accepts File baseDir as an argument, and that it passes that baseDir to the DocumentPersistenceManager constructor
        5. Documents are written to disk as JSON documents. The GSON library is used for this, and a custom JsonDeserializer /JsonSerializer for Documents in included in the program
    3. Converting URIs to location for serialized files
        1. If the user gives your doc a URI of [http://www.yu.edu/documents/doc1](http://www.yu.edu/documents/doc1) the JSON file for that document is stored under [base directory]/www.yu.edu/documents/doc1.json. In other words, the [http://](../NULL) is removed, and then the remaining path of the URI is converted to a file path under the base directory. Each path segment represents a directory, and the name part of the URI represents the name of the file. &quot;.json&quot; is then added to the end of the file name
