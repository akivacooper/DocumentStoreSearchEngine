package edu.yu.cs.com1320.project.stage2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassUtils;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;


public class SomeTest{
@Test
public void simplePushAndPop() {
    Stack<String> s = new StackImpl<>();
    s.push("one");
    s.push("two");
    s.push("three");
    assertEquals(3, s.size());
    assertEquals("three", s.peek());
    assertEquals("three", s.pop());
    assertEquals("two", s.peek());
    assertEquals("two", s.peek());
    assertEquals(2, s.size());
    assertEquals("two", s.pop());
    assertEquals("one", s.pop());
    assertEquals(0, s.size());
}

@Test
public void aLotOfData() {
    Stack<Integer> s = new StackImpl<>();
    for (int i = 0; i < 1000; i++) {
        s.push(i);
        assertEquals((Integer)i, s.peek());
    }
    assertEquals(1000, s.size());
    assertEquals((Integer)999, s.peek());
    for (int i = 999; i >= 0; i--) {
        assertEquals((Integer)i, s.peek());
        assertEquals((Integer)i, s.pop());
    }
    assertEquals(0, s.size());
}
}