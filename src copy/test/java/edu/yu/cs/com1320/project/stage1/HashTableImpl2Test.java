package edu.yu.cs.com1320.project.stage1;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
//import src.main.java.edu.yu.cs.com1320.project.stage1.impl.*;
//import HashTableImpl;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl2Test {

        @Test
        public void hashTableDelNullPut() {
         HashTable<String,Integer> hashTable = new HashTableImpl<String,Integer>();
         
         hashTable.put("Defied", (Integer)22345);
         Integer test1a = hashTable.get("Defied");
         assertEquals(test1a, (Integer)22345);
         hashTable.put("Defied", null);
         Integer test1b = hashTable.get("Defied");
         assertEquals(test1b,null);
         hashTable.put("Oakland", 87123);
         
         Integer test2a = hashTable.get("Oakland");
         assertEquals(test2a, (Integer)87123);
         hashTable.put("Oakland", null);
         hashTable.get("Oakland");
         Integer test2b = hashTable.get("Oakland");
         assertEquals(test2b,null);
         
         hashTable.put("Sanguine", (Integer)4682);
         Integer test3a = hashTable.get("Sanguine");
         assertEquals(test3a, (Integer)4682);
         hashTable.put("Sanguine", null);
         hashTable.get("Sanguine");
         Integer test3b = hashTable.get("Sanguine");
         assertEquals(test3b,null);
        }
        
        @Test
        public void HashEqualButNotEqual() {
         HashTable<String,Integer> hashTable = new HashTableImpl<String,Integer>();
         
         hashTable.put("tensada", 3521);
         hashTable.put("friabili", 1253);
         Integer test1a = hashTable.get("tensada");
         assertEquals(test1a, (Integer)3521);
         Integer test1b = hashTable.get("friabili");
         assertEquals(test1b, (Integer)1253);
         
         hashTable.put("abyz", 8948);
         hashTable.put("abzj", 84980);
         Integer test2a = hashTable.get("abyz");
         assertEquals(test2a, (Integer)8948);
         Integer test2b = hashTable.get("abzj");
         assertEquals(test2b, 84980);
         
         hashTable.put("Siblings", 27128);
         hashTable.put("Teheran", 82172);
         Integer test3a = hashTable.get("Siblings");
         assertEquals(test3a, (Integer)27128);
         Integer test3b = hashTable.get("Teheran");
         assertEquals(test3b, (Integer)82172);
         
        }
     }

