package com.qianzuncheng.sensors;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void emptyQueue() {
        Queue<Integer> q = new LinkedList<>();
        if (q.poll() == null) {
            System.out.println("error");
        }
        System.out.println("go");
    }

    @Test
    public void testPriorityQueue() {
        LinkedList<Integer> q = new LinkedList<>();
        q.offer(3);
        q.offer(10);
        q.offer(7);
        q.offer(1);
        q.offer(5);
        LinkedList<Integer> p = (LinkedList<Integer>) q.clone();
        Collections.sort(p);
        System.out.println(p);
        System.out.println(q);
    }
}