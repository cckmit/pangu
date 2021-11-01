package com.pangu.framework.socket.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class HeaderTest {

    @Test
    public void hasState() {
        assertFalse(Header.valueOf((byte) 0, 0, 0, 0, new Command((short) 0, (short) 0)).hasState(StateConstant.COMMAND_NOT_FOUND));
        assertTrue(Header.valueOf((byte) 0, StateConstant.COMMAND_NOT_FOUND | StateConstant.IDENTITY_EXCEPTION, 0, 0, new Command((short) 0, (short) 0))
                .hasState(StateConstant.COMMAND_NOT_FOUND));
    }

    @Test
    public void addState() {
        Header header = Header.valueOf((byte) 0, 0, 0, 0, new Command((short) 0, (short) 0));
        assertFalse(header.hasState(StateConstant.COMMAND_NOT_FOUND));

        header.addState(StateConstant.COMMAND_NOT_FOUND);

        assertTrue(header.hasState(StateConstant.COMMAND_NOT_FOUND));
    }

    @Test
    public void removeState() {
        Header header = Header.valueOf((byte) 0, StateConstant.COMMAND_NOT_FOUND, 0, 0, new Command((short) 0, (short) 0));
        assertTrue(header.hasState(StateConstant.COMMAND_NOT_FOUND));

        header.removeState(StateConstant.COMMAND_NOT_FOUND);

        assertFalse(header.hasState(StateConstant.COMMAND_NOT_FOUND));
    }

    @Test
    public void isResponse() {
        Header header = Header.valueOf((byte) 0, StateConstant.STATE_RESPONSE, 0, 0, new Command((short) 0, (short) 0));
        assertTrue(header.isResponse());
    }

    @Test
    public void test_write_and_read() {
        byte format = (byte) 1;
        int state = 2;
        long sn = 3;
        long session = 4;

        Command command = new Command((short) 1, (short) 1);
        Header header = Header.valueOf(format, state, sn, session, command);
        ByteBuf buffer = Unpooled.buffer();
        header.write(buffer);
        assertThat(buffer.readableBytes(), is(Header.DEFAULT_HEADER_LENGTH));

        Header out = new Header();
        out.read(buffer);

        assertThat(header.getFormat(), is(format));
        assertThat(header.getState(), is(state));
        assertThat(header.getSn(), is(sn));
        assertThat(header.getSession(), is(session));
        assertEquals(header.getCommand(), command);

        buffer.release();
    }

    @Test
    public void test_write_negative_number() {
        byte format = (byte) -1;
        int state = -2;
        long sn = -3;
        long session = -4;
        Command command = new Command((short) 1, (short) 1);

        Header header = Header.valueOf(format, state, sn, session, command);
        ByteBuf buffer = Unpooled.buffer();
        header.write(buffer);
        assertThat(buffer.readableBytes(), is(Header.DEFAULT_HEADER_LENGTH));

        Header out = new Header();
        out.read(buffer);

        assertThat(header.getFormat(), is(format));
        assertThat(header.getState(), is(state));
        assertThat(header.getSn(), is(sn));
        assertThat(header.getSession(), is(session));
        assertEquals(header.getCommand(), command);

        buffer.release();
    }

    @Test
    public void test_add_error() {
        Header header = Header.valueOf((byte) 0, 0, 0L, 0L, new Command((short) 0, (short) 0));
        header.addError(StateConstant.MANAGED_EXCEPTION);
        assertTrue(header.hasError(StateConstant.MANAGED_EXCEPTION));
        assertEquals(header.getErrorCode(), StateConstant.MANAGED_EXCEPTION);

        header.addError(StateConstant.IDENTITY_EXCEPTION);
        assertTrue(header.hasError(StateConstant.IDENTITY_EXCEPTION));
        assertEquals(header.getErrorCode(), StateConstant.IDENTITY_EXCEPTION);
    }
}