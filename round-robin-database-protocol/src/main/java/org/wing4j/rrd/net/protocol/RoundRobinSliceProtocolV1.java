package org.wing4j.rrd.net.protocol;

import lombok.Data;
import org.wing4j.rrd.debug.DebugConfig;
import org.wing4j.rrd.utils.HexUtils;

import java.nio.ByteBuffer;

/**
 * Created by wing4j on 2017/8/4.
 * 字段自增协议
 */
@Data
public class RoundRobinSliceProtocolV1 extends BaseRoundRobinProtocol {
    ProtocolType protocolType = ProtocolType.SLICE;
    int version = 1;
    MessageType messageType = MessageType.REQUEST;
    String instance = "default";
    String tableName;
    String[] columns = new String[0];
    int pos = 0;
    int size = 0;
    int resultSize = 0;
    long[][] data = new long[0][0];
    int[] timeline = new int[0];

    public void setData(long[][] data){
        this.data = data;
        this.resultSize = data.length;
    }
    @Override
    public ByteBuffer convert() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        //网络传输协议
        //报文长度
        int lengthPos = buffer.position();
        buffer.putInt(0);
        //命令
        buffer = put(buffer, this.protocolType.getCode());
        if (DebugConfig.DEBUG) {
            System.out.println("protocol Type:" + this.protocolType);
        }
        //版本号
        buffer = put(buffer, this.version);
        if (DebugConfig.DEBUG) {
            System.out.println("version:" + this.version);
        }
        //报文类型
        buffer = put(buffer, this.messageType.getCode());
        if (DebugConfig.DEBUG) {
            System.out.println("message Type:" + messageType);
        }
        //应答编码
        buffer = put(buffer, code);
        //应答描述
        buffer = put(buffer, desc);
        //会话ID
        buffer = put(buffer, sessionId);
        //实例名长度
        //实例名
        buffer = put(buffer, instance);
        //表名长度
        //表名
        buffer = put(buffer, this.tableName);
        ///字段数
        buffer = put(buffer, this.columns.length);
        if (DebugConfig.DEBUG) {
            System.out.println("column num:" + this.columns.length);
        }
        //字段名
        for (int i = 0; i < this.columns.length; i++) {
            String column = this.columns[i];
            buffer = put(buffer, column);
        }
        //偏移地址
        buffer = put(buffer, this.pos);
        //记录条数
        buffer = put(buffer, this.size);
        //结果记录条数
        buffer = put(buffer, this.resultSize);
        //数据
        for (int i = 0; i < this.resultSize; i++) {
            for (int j = 0; j < this.columns.length; j++) {
                buffer = put(buffer, this.data[i][j]);
            }
        }
        //时间线
        for (int i = 0; i < this.resultSize; i++) {
            buffer = put(buffer, this.timeline[i]);
        }
        //结束
        //回填,将报文总长度回填到第一个字节
        buffer.putInt(lengthPos, buffer.position() - 4);
        if (DebugConfig.DEBUG) {
            System.out.println(HexUtils.toDisplayString(buffer.array()));
        }
        return buffer;
    }

    @Override
    public void convert(ByteBuffer buffer) {
        if (DebugConfig.DEBUG) {
            System.out.println(HexUtils.toDisplayString(buffer.array()));
        }
        //网络传输协议
        //报文长度
        //命令
        //版本号
        //报文类型
        //应答编码
        this.code = buffer.getInt();
        //应答描述
        this.desc = get(buffer);
        //会话ID
        this.sessionId = get(buffer);
        //实例长度
        //实例
        this.instance = get(buffer);
        //表名长度
        //表名
        this.tableName = get(buffer);
        //字段数目
        int columnNum = buffer.getInt();
        this.columns = new String[columnNum];
        //字段
        for (int i = 0; i < columnNum; i++) {
            columns[i] = get(buffer);
        }
        //偏移地址
        this.pos = buffer.getInt();
        //记录条数
        this.size = buffer.getInt();
        //结果记录条数
        this.resultSize = buffer.getInt();
        //数据
        this.data = new long[this.resultSize][columnNum];
        for (int i = 0; i < this.resultSize; i++) {
            for (int j = 0; j < columnNum; j++) {
                this.data[i][j] = buffer.getLong();
            }
        }
        //时间线
        this.timeline = new int[this.resultSize];
        for (int i = 0; i < this.resultSize; i++) {
            this.timeline[i] = buffer.getInt();
        }
    }
}
