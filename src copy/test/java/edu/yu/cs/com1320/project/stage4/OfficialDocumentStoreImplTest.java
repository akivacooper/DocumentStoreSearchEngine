package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;

import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OfficialDocumentStoreImplTest {

    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;

    //variables to hold possible values for doc3
    private URI uri3;
    private String txt3;

    //variables to hold possible values for doc4
    private URI uri4;
    private String txt4;

    private int bytes1;
    private int bytes2;
    private int bytes3;
    private int bytes4;

    @BeforeEach
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This doc1 plain text string Computer Headphones";

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text doc2 plain String";

        //init possible values for doc3
        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "This is the text of doc3";

        //init possible values for doc4
        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "This is the text of doc4";

        this.bytes1 = this.txt1.getBytes().length;
        this.bytes2 = this.txt2.getBytes().length;
        this.bytes3 = this.txt3.getBytes().length;
        this.bytes4 = this.txt4.getBytes().length;
    }    
    
    private DocumentStore getStoreWithTextAdded() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.TXT);
        return store;
    }

    private DocumentStore getStoreWithBinaryAdded() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()),this.uri4, DocumentStore.DocumentFormat.BINARY);
        return store;
    }

    /*
Every time a document is used, its last use time should be updated to the relative JVM time, as measured in nanoseconds (see java.lang.System.nanoTime().)
A Document is considered to be “used” whenever it is accessed as a result of a call to any part of DocumentStore’s public API. In other words, if it is “put”,
or returned in any form as the result of any “get” or “search” request, or an action on it is undone via any call to either of the DocumentStore.undo methods.
     */

    @Test
    public void testFromPiazza() throws IOException, URISyntaxException{
        URI alteredURI = new URI("http://edu.yu.cs/com1320/project/doc1/");
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(0);
        try {
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), alteredURI, DocumentStore.DocumentFormat.BINARY);
            fail();
        } catch (IllegalArgumentException e){
            return;
        }
        
    }

    @Test
    public void stage4TestSetDocLastUseTimeOnGet() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        Document doc = store.getDocument(this.uri1);
        long first = doc.getLastUseTime();
        doc = store.getDocument(this.uri1);
        long second = doc.getLastUseTime();
        //was last use time updated on the put?
        assertTrue(first < second,"last use time should be changed when the DocumentStore.getDocument method is called");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void stage4TestSetDocLastUseTimeOnPut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        long before = System.nanoTime();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        Document doc = store.getDocument(this.uri1);
        //was last use time set on the put?
        assertTrue(before < doc.getLastUseTime(),"last use time should be after the time at which the document was put");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }
    @Test
    public void stage4TestUpdateDocLastUseTimeOnOverwrite() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        //was last use time updated on the put?
        long before = System.nanoTime();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        Document doc = store.getDocument(this.uri1);
        assertTrue(before < doc.getLastUseTime(),"last use time should be after the time at which the document was put");
        before = System.nanoTime();
        //was last use time updated on overwrite?
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        Document doc2 = store.getDocument(this.uri1);
        assertTrue(before < doc2.getLastUseTime(),"last use time should be after the time at which the document was overwritten");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearch() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<Document> results = store.search("Computer");
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the search?
        assertTrue(before < doc.getLastUseTime(),"last use time of search result doc should be after the time at which the document was put");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }
    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearchByPrefix() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<Document> results = store.searchByPrefix("Comput");
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the searchByPrefix?
        assertTrue(before < doc.getLastUseTime(),"last use time of search result should be after the time at which the document was put");
        
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    
    }

    /**
     * test max doc count via put
     */
    @Test
    public void stage4TestMaxDocCountViaPut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.BINARY);
        //uri1 and uri2 should both be gone, having been pushed out by 3 and 4
        
        assertTrue(getFullPath(uri1).exists());
        assertTrue(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        assertFalse(getFullPath(uri4).exists());

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);



    }

    /**
     * test max doc count via search
     */
    @Test
    public void stage4TestMaxDocCountViaSearch() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(3);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        //all 3 should still be in memory
       
        assertFalse(getFullPath(uri1).exists());
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        //"touch" uri1 via a search
        store.search("doc1");
        //add doc4, doc2 should be pushed out, not doc1
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        assertFalse(getFullPath(uri1).exists());
        assertFalse(getFullPath(uri4).exists());
        assertFalse(getFullPath(uri3).exists());
        //uri2 should've been pushed out of memory
        assertTrue(getFullPath(uri2).exists());

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    /**
     * test undo after going over max doc count
     */
    @Test
    public void stage4TestUndoAfterMaxDocCount() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentCount(3);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.BINARY);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.BINARY);
        //all 3 should still be in memory
        assertFalse(getFullPath(uri1).exists());
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        //add doc4, doc1 should be pushed out to disk
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.BINARY);
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri4).exists());
        assertFalse(getFullPath(uri3).exists());
        //uri1 should've been pushed out to disk
        assertTrue(getFullPath(uri1).exists());
        //undo the put - should eliminate doc4, uri2 uri 1 and uri3 should be in memroy 
        store.undo();
        assertFalse(getFullPath(uri1).exists());
        assertNull(store.getDocument(uri4));
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }


    /**
     * test max doc bytes via put
     */
    @Test
    public void stage4TestMaxDocBytesViaPut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        
        assertTrue(getFullPath(uri1).exists());
        assertTrue(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        assertFalse(getFullPath(uri4).exists());
        //uri1 and uri2 should both be on disk, having been pushed out by 3 and 4
       
    
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);

    }

    /**
     * test max doc bytes via search
     */
    @Test
    public void stage4TestMaxDocBytesViaSearch() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2 + this.bytes3 + 10);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        //all 3 should still be in memory
        assertFalse(getFullPath(uri1).exists());
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        //"touch" uri1 via a search
        store.search("doc1");
        //add doc4, doc2 should be pushed out, not doc1
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        assertFalse(getFullPath(uri1).exists());
        assertFalse(getFullPath(uri3).exists());
        assertFalse(getFullPath(uri4).exists());
        //uri2 should've been pushed out of memory
        assertTrue(getFullPath(uri2).exists());
        
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    
    }

    /**
     * test undo after going over max bytes
     */
    @Test
    public void stage4TestUndoAfterMaxBytes() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2 + this.bytes3);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        //all 3 should still be in memory
        assertFalse(getFullPath(uri1).exists());
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        //add doc4, doc1 should be pushed out
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        assertFalse(getFullPath(uri4).exists());
        //uri1 should've been pushed out of memory
        assertTrue(getFullPath(uri1).exists());
        //undo the put - should eliminate doc4, and uri2 uri3 and uri1 should be in memory
        store.undo();
        assertNull(store.getDocument(this.uri4),"uri4 should be gone due to the undo");
        assertFalse(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        assertFalse(getFullPath(uri1).exists());
    
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);

    }

    /**
     * test going over max docs only when both max docs and max bytes are set
     */
    @Test
    public void stage4TestMaxDocsWhenDoubleMaxViaPut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1*10);
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        //uri1 and uri2 should both be gone on disk, and 3 and 4 should be in memory
        
        
        assertFalse(getFullPath(uri4).exists());
        assertFalse(getFullPath(uri3).exists());
        assertTrue(getFullPath(uri1).exists());
        assertTrue(getFullPath(uri2).exists());
    
        
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
        
    
    }

    /**
     * test going over max bytes only when both max docs and max bytes are set
     */
    @Test
    public void stage4TestMaxBytesWhenDoubleMaxViaPut() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2);
        store.setMaxDocumentCount(20);
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);
        //uri1 and uri2 should both be gone in memory, having been pushed out by 3 and 4
        assertTrue(getFullPath(uri1).exists());
        assertTrue(getFullPath(uri2).exists());
        assertFalse(getFullPath(uri3).exists());
        assertFalse(getFullPath(uri4).exists());
    
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    
    }


    //stage 3 tests

    @Test
    public void stage3Search() throws IOException {
        List<Document> results = this.search(this.getStoreWithTextAdded(),"plain",2);
        assertTrue(this.containsDocWithUri(results,this.uri1),"Result set should've included " + this.uri1);
        assertTrue(this.containsDocWithUri(results,this.uri2),"Result set should've included " + this.uri2);
        this.search(this.getStoreWithTextAdded(),"missing",0);
    
    }
    @Test
    public void stage3SearchBinary() throws IOException {
        List<Document> results = this.search(this.getStoreWithBinaryAdded(),"plain",2);
        assertTrue(this.containsDocWithUri(results,this.uri1),"Result set should've included " + this.uri1);
        assertTrue(this.containsDocWithUri(results,this.uri2),"Result set should've included " + this.uri2);
        this.search(this.getStoreWithBinaryAdded(),"missing",0);
    }

    @Test
    public void stage3DeleteAllTxt() throws IOException {
        DocumentStore store = this.getStoreWithTextAdded();
        String keyword = "plain";
        store.deleteAll(keyword);
        List<Document> results = store.search(keyword);
        URI[] absent = {this.uri1,this.uri2,this.uri3,this.uri4};
        URI[] present = new URI[0];
        this.checkContents(results,present,absent);

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void stage3DeleteAllBinary() throws IOException {
        DocumentStore store = this.getStoreWithBinaryAdded();
        String keyword = "Headphones";
        store.deleteAll(keyword);
        List<Document> results = store.search(keyword);
        URI[] absent = {this.uri1,this.uri2,this.uri3,this.uri4};
        URI[] present = new URI[0];
        this.checkContents(results,present,absent);

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void stage3SearchTxtByPrefix() throws IOException {
        this.stage3SearchByPrefix(this.getStoreWithTextAdded());
    }

    @Test
    public void stage3SearchBinaryByPrefix() throws IOException {
        this.stage3SearchByPrefix(this.getStoreWithBinaryAdded());
    }

    @Test
    public void stage3DeleteAllWithPrefix() throws IOException {
        DocumentStore store = this.getStoreWithTextAdded();
        //delete all starting with thi
        store.deleteAllWithPrefix("thi");
        List<Document> results = store.searchByPrefix("thi");
        assertEquals(0,results.size(),"search should've returned 0 results");
        URI[] present = new URI[0];
        URI[] absent = {this.uri1,this.uri2,this.uri3,this.uri4};
        this.checkContents(results,present,absent);

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);

    }

    private List<Document> search(DocumentStore store, String keyword, int expectedMatches){
        List<Document> results = store.search(keyword);
        assertEquals(expectedMatches,results.size(),"expected " + expectedMatches + " matches, received " + results.size());
        return results;
    }

    private boolean containsDocWithUri(List<Document> docs, URI uri){
        for(Document doc : docs){
            if(doc.getKey().equals(uri)){
                return true;
            }
        }
        return false;
    }

    private void checkContents(List<Document> results, URI[] present, URI[] absent){
        for(URI uri : present){
            if(!this.containsDocWithUri(results, uri)){
                fail(uri + " should be in the result set, but is not");
            }
        }
        for(URI uri : absent){
            if(this.containsDocWithUri(results, uri)){
                fail(uri + " should NOT be in the result set, but is");
            }
        }
    }

    private void stage3SearchByPrefix(DocumentStore store){
        List<Document> results = store.searchByPrefix("str");
        assertEquals(2,results.size(),"expected 2 match, received " + results.size());
        URI[] present = {this.uri1,this.uri2};
        URI[] absent = {this.uri3,this.uri4};
        this.checkContents(results,present,absent);

        results = store.searchByPrefix("comp");
        assertEquals(1,results.size(),"expected 1 match, received " + results.size());
        URI[] present2 = {this.uri1};
        URI[] absent2 = {this.uri3,this.uri4,this.uri2};
        this.checkContents(results,present2,absent2);

        results = store.searchByPrefix("doc2");
        assertEquals(1,results.size(),"expected 1 match, received " + results.size());
        URI[] present3 = {this.uri2};
        URI[] absent3 = {this.uri3,this.uri4,this.uri1};
        this.checkContents(results,present3,absent3);

        results = store.searchByPrefix("blah");
        assertEquals(0,results.size(),"expected 0 match, received " + results.size());
    }

    //stage 1 tests
        @Test
    public void testPutBinaryDocumentNoPreviousDocAtURI() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testPutTxtDocumentNoPreviousDocAtURI() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testPutDocumentWithNullArguments() throws IOException{
        DocumentStore store = new DocumentStoreImpl();
        try {
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), null, DocumentStore.DocumentFormat.TXT);
            fail("null URI should've thrown IllegalArgumentException");
        }catch(IllegalArgumentException e){}
        try {
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, null);
            fail("null format should've thrown IllegalArgumentException");
        }catch(IllegalArgumentException e){}

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testPutNewVersionOfDocumentBinary() throws IOException {
        //put the first version
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
        Document doc1 = store.getDocument(this.uri1);
        assertArrayEquals(this.txt1.getBytes(),doc1.getDocumentBinaryData(),"failed to return correct binary text");

        //put the second version, testing both return value of put and see if it gets the correct text
        int expected = doc1.hashCode();
        returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.BINARY);

        assertEquals(expected, returned,"should return hashcode of the old document");
        assertArrayEquals(this.txt2.getBytes(),store.getDocument(this.uri1).getDocumentBinaryData(),"failed to return correct data");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testPutNewVersionOfDocumentTxt() throws IOException {
        //put the first version
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");

        //put the second version, testing both return value of put and see if it gets the correct text
        returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(Utils.calculateHashCode(this.uri1, this.txt1,null) == returned,"should return hashcode of old text");
        assertEquals(this.txt2,store.getDocument(this.uri1).getDocumentTxt(),"failed to return correct text");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testGetTxtDoc() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals(this.txt1,store.getDocument(this.uri1).getDocumentTxt(),"did not return a doc with the correct text");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testGetTxtDocAsBinary() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertFalse(getFullPath(uri1).exists());

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testGetBinaryDocAsBinary() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
        assertArrayEquals(this.txt2.getBytes(),store.getDocument(this.uri2).getDocumentBinaryData(),"failed to return correct binary array");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testGetBinaryDocAsTxt() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.BINARY);
        assertTrue(returned == 0);
        assertNull(store.getDocument(this.uri2).getDocumentTxt(),"binary doc should return null for text");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testDeleteDoc() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(this.uri1);
        assertNull(store.getDocument(this.uri1),"calling get on URI from which doc was deleted should've returned null");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    @Test
    public void testDeleteDocReturnValue() throws IOException {
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        //should return true when deleting a document
        assertEquals(true,store.deleteDocument(this.uri1),"failed to return true when deleting a document");
        //should return false if I try to delete the same doc again
        assertEquals(false,store.deleteDocument(this.uri1),"failed to return false when trying to delete that which was already deleted");
        //should return false if I try to delete something that was never there to begin with
        assertEquals(false,store.deleteDocument(this.uri2),"failed to return false when trying to delete that which was never there to begin with");

        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.deleteDocument(uri3);
        store.deleteDocument(uri4);
    }

    private File getFullPath(URI uri){
        File fullPath;
        if (uri.getAuthority() == null){
            fullPath = new File(System.getProperty("user.dir") + File.separator + uri.getPath() + ".json");
        } else {
            fullPath = new File(System.getProperty("user.dir") + File.separator + uri.getAuthority() + uri.getPath() + ".json");
        }

        return fullPath;
    }

}