package edu.yu.cs.com1320.project.stage5.impl;

import javax.xml.bind.DatatypeConverter;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.Cleaner;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * created by the document store and, within the documentstore it is given to the BTree via a call to BTree.setPersistenceManager
 * DocumentPersistenceManager should use instances of com.google.gson.JsonSerializer<Document> and com.google.gson.JsonDeserializer<Document> to (de)serialize from/to disk.
 * Documents must be written to disk as JSON documents. You must use the GSON library for this.
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

    private JsonDeserializer<Document> documentDeserializer;
    private JsonSerializer<DocumentImpl> documentSerializer;
    private String baseDirectory;

    
    
    /**
    public static void main (String[] args) throws IOException, URISyntaxException{

        DocumentPersistenceManager myManager = new DocumentPersistenceManager(new File ("/Users/me/Desktop/File1"));
        DocumentPersistenceManager myManager2 = new DocumentPersistenceManager(null);

        
        List<URI> uriHolder = new ArrayList<URI>();

        uriHolder.add(new URI ("google.com"));
        uriHolder.add(new URI ("http://google.com"));
        uriHolder.add(new URI ("/Users/me/google.com"));
        uriHolder.add(new URI ("http://Users/me/google.com"));
        uriHolder.add(new URI ("http://www.yu.edu/documents/doc1"));
        uriHolder.add(new URI ("http://edu.yu.cs/com1320/project/doc1"));
        uriHolder.add(new URI("http://www.yu.edu.documents.doc1"));
        uriHolder.add(new URI("http://website.com/doc1"));

        //------------------------------------------------------------------------------------------------------

        
        myManager.serialize(uriHolder.get(5), new DocumentImpl (uriHolder.get(5), "text"));
        myManager2.serialize(uriHolder.get(5), new DocumentImpl (uriHolder.get(5), "text"));

        
        DocumentImpl doc1 = (DocumentImpl) myManager.deserialize(uriHolder.get(5));
        DocumentImpl doc2 = (DocumentImpl) myManager2.deserialize(uriHolder.get(5));

        System.out.println(doc1);
        System.out.println(doc2);


        /**
         for (URI nextURI : uriHolder){
         testURI(nextURI)
         }
        

        for (URI nextUri : uriHolder){
            checkToPath(myManager, nextUri);
        }
    }
    */

    /**
    private static void testURI (URI uri) throws URISyntaxException{
        System.out.println();
        System.out.println("**********************************************");
        System.out.println("New URI: " + uri);
        
        System.out.println();
        System.out.println("getAuthority(): " + uri.getAuthority());
        System.out.println("getRawAuthority(): " + uri.getRawAuthority());
        
        System.out.println();
        System.out.println("getFragment(): " + uri.getFragment());
        System.out.println("getRawFragment(): " + uri.getRawFragment());

        
        System.out.println();
        System.out.println("getFragment(): " + uri.getFragment());
        System.out.println("getRawFragment(): " + uri.getRawFragment());

       
        System.out.println();
        System.out.println("getHost(): " + uri.getHost());

        System.out.println();
        System.out.println("getPath(): " + uri.getPath());
        System.out.println("getRawPath(): " + uri.getRawPath());

        System.out.println();
        System.out.println("getPort(): " + uri.getPort());

        System.out.println();
        System.out.println("getQuery(): " + uri.getQuery());
        System.out.println("getRawQuery(): " + uri.getRawQuery());

        System.out.println();
        System.out.println("getScheme(): " + uri.getScheme());

        System.out.println();
        System.out.println("getSchemeSpecificPart(): " + uri.getSchemeSpecificPart());
        System.out.println("getRawSchemeSpecificPart(): " + uri.getRawSchemeSpecificPart());

        System.out.println();
        System.out.println("getUserInfo(): " + uri.getUserInfo());

        System.out.println();
        System.out.println("toASCIIString(): " + uri.toASCIIString());

        System.out.println();
        System.out.println("toString(): " + uri.toString());
        
        System.out.println();
        System.out.println("Simple print of parsingServerAuthority: " + uri.parseServerAuthority());

        System.out.println();
        if (uri.getAuthority() == null){
            System.out.println("Authority + Path: " + uri.getPath());
        } else {
            System.out.println("Authority + Path: " + uri.getAuthority() + uri.getPath());
        }
    }
    */

    /**
    private static void checkToPath (DocumentPersistenceManager manager, URI uri){

        System.out.println();
        System.out.println("Checking toPath() method");
        System.out.println("The URI is: " + uri);
        System.out.println("The converted URI is: " + manager.getFullPath(uri));
        System.out.println("The path of the converted URI is: " + manager.getFullPath(uri).toPath());
        System.out.println("The convertedURI and its path are the same: " + manager.getFullPath(uri).toString().equals(manager.getFullPath(uri).toPath().toString()));

    }
    */

    public DocumentPersistenceManager(File baseDir){
        /**
         * If null,
            * use the working directory of your application
         * Else,
            * Use that directory instead
         */
        if (baseDir == null){
            baseDirectory = System.getProperty("user.dir");

        } else {
            this.baseDirectory = baseDir.getAbsolutePath();
        }

        documentSerializer = new JsonSerializer<DocumentImpl>(){  
            @Override
            public JsonElement serialize(DocumentImpl document, Type typeOfSrc, JsonSerializationContext context) {
                
                JsonObject serializedDocuemntAsJson = new JsonObject();

                serializedDocuemntAsJson.add("map", context.serialize(document.getWordMap()));
                serializedDocuemntAsJson.addProperty("uri", document.getKey().toString());

                String docText = document.getDocumentTxt();
                byte[] byteArray = document.getDocumentBinaryData();

                if (docText!=null){
                    serializedDocuemntAsJson.addProperty("stringContent", docText);
                } else {
                    String byteArrayAsBase64String = DatatypeConverter.printBase64Binary(byteArray);
                    serializedDocuemntAsJson.addProperty("byteContent", byteArrayAsBase64String);
                }
                        
                return serializedDocuemntAsJson;
            }
        };
        documentDeserializer = new JsonDeserializer<Document>(){

            @Override
            public Document deserialize(JsonElement jsonDocumentElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                
                URI uri;
                String docText;
                byte[] byteArray;
                HashMap<String, Integer> wordCountMap;
                Document docToReturn;

                JsonObject jsonDocumentObject = jsonDocumentElement.getAsJsonObject();
                
                try {
                    uri = new URI(jsonDocumentObject.get("uri").getAsString());
                } catch (URISyntaxException e){
                    throw new JsonParseException("Attempting to deserialize document. Stored URI violates RFC 2396");
                }

                Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
                wordCountMap = context.deserialize(jsonDocumentObject.get("map"), type);

                if (jsonDocumentObject.has("stringContent")){
                    docText = jsonDocumentObject.get("stringContent").getAsString();
                    docToReturn = new DocumentImpl(uri, docText);
                    docToReturn.setWordMap(wordCountMap);
                    docToReturn.setLastUseTime(System.nanoTime());
                } else {
                    byteArray = DatatypeConverter.parseBase64Binary(jsonDocumentObject.get("byteContent").getAsString());
                    docToReturn = new DocumentImpl(uri, byteArray);
                    docToReturn.setWordMap(wordCountMap);
                    docToReturn.setLastUseTime(System.nanoTime());
                }

                return docToReturn;

            }
            
        };
        

    }

    @Override
    public void serialize(URI uri, Object val) throws IOException {

        /**
         * TODO
         * You serialize three things:
            * 1. the contents of the document (String or binary)
            * 2. the URI
            * 3. the wordcount map.
         * I'm not really sure how to do this.
         * You do not serialize the LUT
         */

         //System.out.println("Serlizer called on: " + uri);

         if (val == null){
             throw new IllegalArgumentException("You can't serialize nothing");
         }
         
         try{
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(DocumentImpl.class, documentSerializer);
            Gson customGsonForSerialization = gsonBuilder.create();
            String serializedDocumentAsJson = customGsonForSerialization.toJson((DocumentImpl)val);
            
             
            File fullPath = getFullPath(uri);
            File JsonFileParent = fullPath.getParentFile();

            String JsonFileParentString = "" + JsonFileParent;
            String fullPathString = fullPath.toString();
            String fullPathStringWithoutExtension = fullPathString.substring(0,((int)(fullPathString.length()-5)));

            /** 
            System.out.println(JsonFileParentString);
            System.out.println(fullPathString);
            System.out.println(fullPathStringWithoutExtension);

            System.out.println("Last char of fullPathWithoutExtension: " + (fullPathStringWithoutExtension.charAt(fullPathStringWithoutExtension.length() - 1)));
            System.out.println("File seperator is: " + File.separatorChar);
            */

            if (fullPathStringWithoutExtension.charAt(fullPathStringWithoutExtension.length() - 1) == File.separatorChar){
                throw new IllegalArgumentException();
            }
            

            JsonFileParent.mkdirs();
            FileWriter fileWriter = new FileWriter(fullPath);
            fileWriter.write(serializedDocumentAsJson);
            fileWriter.close();
         } catch (IOException e){
            throw new IOException("Something went wrong");
         }
        




    }

    private File getFullPath(URI uri){
        File fullPath;
        if (uri.getAuthority() == null){
            fullPath = new File(baseDirectory + File.separator + uri.getPath() + ".json");
        } else {
            fullPath = new File(baseDirectory + File.separator + uri.getAuthority() + uri.getPath() + ".json");
        }

        return fullPath;
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
       //TODO
       /**
        * TODO
        * Set the wordmap, calling setWordMap
        * Update time
        * Call delete with the URI
        */
        try{
            GsonBuilder gsonBuilder = new GsonBuilder();

            gsonBuilder.registerTypeAdapter(Document.class, documentDeserializer);

            Gson customGson = gsonBuilder.create();  
            
            //getFile as jSon string
            //delete the file
            File fullPath = getFullPath(uri);
            BufferedReader reader = Files.newBufferedReader(fullPath.toPath());
            
            
            Document deserializedJsonAsDoc = customGson.fromJson(reader, Document.class);
            delete(uri);
            return deserializedJsonAsDoc;
        } catch (IOException e){
            if (e instanceof FileNotFoundException){
                return null;
            } else {
                throw new IOException();
            }
        }
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return getFullPath(uri).delete();
    }

}