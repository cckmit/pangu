package com.pangu.framework.socket.handler;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultDispatcherTest {

    @Test
    public void calDefaultThreadCount() {
        int i = DefaultDispatcher.calDefaultThreadCount(1);
        assertThat(i, is(1));
        i = DefaultDispatcher.calDefaultThreadCount(2);
        assertThat(i, is(1));
        i = DefaultDispatcher.calDefaultThreadCount(3);
        assertThat(i, is(3));
        i = DefaultDispatcher.calDefaultThreadCount(4);
        assertThat(i, is(3));
        i = DefaultDispatcher.calDefaultThreadCount(5);
        assertThat(i, is(3));
        i = DefaultDispatcher.calDefaultThreadCount(6);
        assertThat(i, is(3));
        i = DefaultDispatcher.calDefaultThreadCount(7);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(8);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(9);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(10);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(11);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(12);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(13);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(14);
        assertThat(i, is(7));
        i = DefaultDispatcher.calDefaultThreadCount(15);
        assertThat(i, is(15));
        i = DefaultDispatcher.calDefaultThreadCount(16);
        assertThat(i, is(15));
        i = DefaultDispatcher.calDefaultThreadCount(17);
        assertThat(i, is(15));
        i = DefaultDispatcher.calDefaultThreadCount(18);
        assertThat(i, is(15));
        i = DefaultDispatcher.calDefaultThreadCount(19);
        assertThat(i, is(15));
        i = DefaultDispatcher.calDefaultThreadCount(20);
        assertThat(i, is(15));
        i = DefaultDispatcher.calDefaultThreadCount(21);
        assertThat(i, is(15));
    }
}