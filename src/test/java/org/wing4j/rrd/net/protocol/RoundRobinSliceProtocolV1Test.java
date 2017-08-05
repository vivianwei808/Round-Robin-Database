package org.wing4j.rrd.net.protocol;

import org.junit.Assert;
import org.junit.Test;
import org.wing4j.rrd.core.TableStatus;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by 面试1 on 2017/8/4.
 */
public class RoundRobinSliceProtocolV1Test {

    @Test
    public void testConvert() throws Exception {
        RoundRobinSliceProtocolV1 format = new RoundRobinSliceProtocolV1();
        format.setTableName("table1");
        format.setColumns(new String[]{"request", "response"});
        format.setPos(3);
        format.setSize(2);
        long[][] data = new long[][]{
                {1, 2},
                {3, 4}
        };
        format.setData(data);
        ByteBuffer buffer = format.convert();
        buffer.flip();
        int size = buffer.getInt();
        int type = buffer.getInt();
        int version = buffer.getInt();
        int messageType = buffer.getInt();
        Assert.assertEquals(MessageType.REQUEST.getCode(), messageType);
        RoundRobinSliceProtocolV1 format2 = new RoundRobinSliceProtocolV1();
        format2.convert(buffer);
        Assert.assertEquals("table1", format2.getTableName());
        Assert.assertEquals("request", format2.getColumns()[0]);
        Assert.assertEquals("response", format2.getColumns()[1]);
        Assert.assertEquals(3, format2.getPos());
        Assert.assertEquals(2, format2.getSize());
        Assert.assertEquals(1, format2.getData()[0][0]);
        Assert.assertEquals(2, format2.getData()[0][1]);
        Assert.assertEquals(3, format2.getData()[1][0]);
        Assert.assertEquals(4, format2.getData()[1][1]);
    }

    @Test
    public void testConvert1() throws Exception {

    }
}