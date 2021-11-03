package edu.yu.cs.com1320.project.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import edu.yu.cs.com1320.project.MinHeap;

/**
 * Beginnings of a MinHeap, for Stage 4 of project. Does not include the additional data structure or logic needed to reheapify an element after its last use time changes.
 * @param <E>
 */
public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>{
    protected E[] elements;
    protected int count = 0;

    public MinHeapImpl(){
        elements = (E[]) new Comparable[1];
    }

    public void reHeapify(E element){
        
        int index = getArrayIndex(element);
        
        upHeap(index);
        downHeap(index);
    }

    protected int getArrayIndex(E element){
        /** 
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: GET ARRAY INDEX***");
        */

        for (int i = 1; i <= count; i++){            
            
            /**
            System.out.println();
            System.out.println("At index: " + i);
            System.out.println("Here is: " + elements[i]);
            System.out.println("Comparing to: " + element);
            */
            
            if (elements[i].equals(element)){
                return i;
            }
        }

        //System.out.println("Couldn't find it");

        throw new NoSuchElementException();

    }

    protected void doubleArraySize(){
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: DOUBLE ARRAY SIZE***");
        printArray();
        */
        
        E[] newArray = elements;
        elements = (E[]) new Comparable[elements.length * 2];
        
        for (int i = 0; i < newArray.length; i++){
            elements[i] = newArray[i];
        }
        /**
        System.out.println("Doubled array");
        printArray();
        */

    }

    protected boolean isEmpty() {
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: ISEMPTY***");
        */
        
        return this.count == 0;
    }

    /**
     * is elements[i] > elements[j]?
     */
    protected boolean isGreater(int i, int j) {

        return this.elements[i].compareTo(this.elements[j]) > 0;
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */
    protected void swap(int i, int j) {
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: SWAP (1o2)***");
        printArray();
        */
        
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;

        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: SWAP (2o2)***");
        printArray();
        */
    }

    /**
     * while the key at index k is less than its
     * parent's key, swap its contents with its parent’s
     */
    protected void upHeap(int k) {
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: UPHEAP***");
        */
        
        while (k > 1 && this.isGreater(k / 2, k)) {
            //System.out.println("Calling UpHeap on index: " + k);
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    protected void downHeap(int k) {
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: DOWNHEAP(1o2)***");
        printArray();
        */
       
        while (2 * k <= this.count) {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1)) {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j)) {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }

        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: DOWNHEAP(2o2)***");
        printArray();
        */
    }

    public void insert(E x) {

        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: INSERT (1o3)***");
        */

        // double size of array if necessary
        if (this.count >= this.elements.length - 1) {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;

        /** 
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: INSERT (2o3)***");
        System.out.println("Added document to the bottom of the heap");
        printArray();
        */
        //percolate it up to maintain heap order property
        this.upHeap(this.count);

        /** 
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: INSERT (3o3)***");
        printArray();
        */

    }

    public E remove() {
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: REMOVE (1o2)***");
        printArray();
        */
        
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        
        /**
        System.out.println();
        System.out.println("***CLASS: HEAPIMPL***");
        System.out.println("***METHOD: REMOVE (2o2)***");
        printArray();
        */
        
        return min;
    }

    
    private void printArray(){

        System.out.println("Current make up of heap array:");
        for (int i = 0; i < elements.length; i++){
            if (elements[i] != null){
                System.out.print("" + i + ". filled ");
            } else {
                System.out.print("" + i + ". null ");
            }
        }
        System.out.println();


    }
    
    /**
    @Override
    public List<E> getArray() {
        if (elements == null){
            printArray();
            System.out.println("Elements is null");
            return new ArrayList<E>();
        } else {
            List<E> setToReturn = new ArrayList<E>();
        
            int i = 0;

            for (E nextE : elements){
                setToReturn.add(i, nextE);
                i++;
            }

            return setToReturn;
        }
    }
    */
    

}
