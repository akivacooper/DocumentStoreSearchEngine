package edu.yu.cs.com1320.project.impl;

import java.util.ArrayList;
import java.util.List;

import edu.yu.cs.com1320.project.Stack;

/**
 * @param <T>
 */
public class StackImpl<T> implements Stack<T>{

    List<T> stack; 

    public StackImpl (){

        stack = new ArrayList<T>();

    }
    
    /**
     * @param element object to add to the Stack
     */
    public void push(T element){
        if (element == null){
            throw new IllegalArgumentException("NULL ELEMENT PASSED AS PARAMETER");
        } else {
            stack.add(0, element);
        }


    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){

        if (size() > 0) {
            return stack.remove(0);
        } else {
            return null;
        }
    

    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek(){

        if (size() > 0) {
            return stack.get(0);
        } else {
            return null;
        }

    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size(){

        return stack.size();
    }

}
