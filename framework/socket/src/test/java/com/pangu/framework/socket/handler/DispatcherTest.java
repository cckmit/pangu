package com.pangu.framework.socket.handler;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DispatcherTest {

    @Test
    public void calDefaultThreadCount() {
        int i = Dispatcher.calDefaultThreadCount(1);
        assertThat(i, is(1));
        i = Dispatcher.calDefaultThreadCount(2);
        assertThat(i, is(1));
        i = Dispatcher.calDefaultThreadCount(3);
        assertThat(i, is(3));
        i = Dispatcher.calDefaultThreadCount(4);
        assertThat(i, is(3));
        i = Dispatcher.calDefaultThreadCount(5);
        assertThat(i, is(3));
        i = Dispatcher.calDefaultThreadCount(6);
        assertThat(i, is(3));
        i = Dispatcher.calDefaultThreadCount(7);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(8);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(9);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(10);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(11);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(12);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(13);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(14);
        assertThat(i, is(7));
        i = Dispatcher.calDefaultThreadCount(15);
        assertThat(i, is(15));
        i = Dispatcher.calDefaultThreadCount(16);
        assertThat(i, is(15));
        i = Dispatcher.calDefaultThreadCount(17);
        assertThat(i, is(15));
        i = Dispatcher.calDefaultThreadCount(18);
        assertThat(i, is(15));
        i = Dispatcher.calDefaultThreadCount(19);
        assertThat(i, is(15));
        i = Dispatcher.calDefaultThreadCount(20);
        assertThat(i, is(15));
        i = Dispatcher.calDefaultThreadCount(21);
        assertThat(i, is(15));
    }
}