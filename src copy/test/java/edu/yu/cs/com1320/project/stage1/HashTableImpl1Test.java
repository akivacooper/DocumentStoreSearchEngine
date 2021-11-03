package edu.yu.cs.com1320.project.stage1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
//import src.main.java.edu.yu.cs.com1320.project.stage1.impl.*;
//import HashTableImpl;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl1Test{



@Test
  public void hashTableImplSimplePutAndGet() {
   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
   hashTable.put(1,2);
   hashTable.put(3,6);
   hashTable.put(7,14);
   int x = hashTable.get(1);
   int y = hashTable.get(3);
   int z = hashTable.get(7);
   assertEquals(2, x);
   assertEquals(6, y);
   assertEquals(14, z);
   
   
    
  }
  
  @Test
  public void hashTableImplALotOfInfoTest() {
   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
   for (int i = 0; i<1000; i++) {
    hashTable.put(i,2*i);
    //System.out.println("For loop ran at: " + i);
   }
   
   //System.out.println("For loop is done");
   int aa = hashTable.get(450);
   assertEquals(900, aa);
  }
  
  
  @Test
  public void hashTableImplCollisionTest() {
   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
   hashTable.put(1, 9);
   hashTable.put(6,12);
   hashTable.put(11,22);
   int a = hashTable.get(1);
   int b = hashTable.get(6);
   int c = hashTable.get(11);
   assertEquals(9, a);
   assertEquals(12, b);
   assertEquals(22, c);
  }
  
  @Test
  public void hashTableImplReplacementTest() {
   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
   hashTable.put(1,2);
   int a = hashTable.put(1, 3);
   assertEquals(2, a);
   int b = hashTable.put(1, 4);
   assertEquals(3,b);
   int c = hashTable.put(1, 9);
   assertEquals(4, c);
  }
}