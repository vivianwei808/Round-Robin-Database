package org.wing4j.rrd.server.aio;

import org.wing4j.rrd.*;
import org.wing4j.rrd.core.Table;
import org.wing4j.rrd.debug.DebugConfig;
import org.wing4j.rrd.net.protocol.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by wing4j on 2017/8/1.
 */
public class RoundRobinReadHandler implements CompletionHandler<Integer, ByteBuffer> {
    static Logger LOGGER = Logger.getLogger(RoundRobinReadHandler.class.getName());
    AsynchronousSocketChannel channel;
    RoundRobinDatabase database;

    public RoundRobinReadHandler(AsynchronousSocketChannel channel, RoundRobinDatabase database) {
        this.channel = channel;
        this.database = database;
    }

    static final boolean DEBUG = true;

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        if (result < 1) {
            return;
        }
        attachment.flip();
        int size = attachment.getInt();
        System.out.println(size);
        attachment = ByteBuffer.allocate(size);
        Future future = channel.read(attachment);
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        attachment.flip();
        if (attachment.remaining() < size) {
            System.out.println("无效报文");
            return;
        }
        //命令类型
        int type = attachment.getInt();
        ProtocolType protocolType = ProtocolType.valueOfCode(type);
        //通信协议版本号
        int version = attachment.getInt();
        if(DebugConfig.DEBUG){
            System.out.println("命令:" + protocolType.getDesc() + "." + version);
        }
        int messageType = attachment.getInt();
        if(DebugConfig.DEBUG){
            System.out.println("报文类型:" + MessageType.valueOfCode(messageType));
        }
       if(protocolType == ProtocolType.TABLE_METADATA && version == 1){//获取数据数量
           //读取到数据流
           RoundRobinTableMetadataProtocolV1 protocol = new RoundRobinTableMetadataProtocolV1();
           protocol.convert(attachment);
           try {
               //进行合并视图操作
               Table table = this.database.getTable(protocol.getTableName());
               protocol.setTableName(table.getMetadata().getName());
               protocol.setDataSize(table.getSize());
               protocol.setStatus(table.getMetadata().getStatus());
               protocol.setColumns(table.getMetadata().getColumns());
           }finally {
           }
           protocol.setMessageType(MessageType.RESPONSE);
           //写应答数据
           ByteBuffer resultBuffer = protocol.convert();
           resultBuffer.flip();
           //注册异步写入返回信息
           channel.write(resultBuffer, resultBuffer, new RoundRobinWriteHandler(channel, this.database));
       }else if(protocolType == ProtocolType.SLICE && version == 1){
           //读取到数据流
           RoundRobinSliceProtocolV1 protocol = new RoundRobinSliceProtocolV1();
           protocol.convert(attachment);
           try {
               //进行合并视图操作
               RoundRobinConnection connection = this.database.open();
               RoundRobinView view = connection.slice(protocol.getTableName(), protocol.getPos(), protocol.getSize(), protocol.getColumns());
               protocol.setPos(view.getTime());
               protocol.setSize(view.getData().length);
               protocol.setData(view.getData());
               protocol.setColumns(view.getMetadata().getColumns());
               protocol.setMessageType(MessageType.RESPONSE);
           } catch (IOException e) {
               e.printStackTrace();
           } finally {
           }
           //写应答数据
           ByteBuffer resultBuffer = protocol.convert();
           resultBuffer.flip();
           //注册异步写入返回信息
           channel.write(resultBuffer, resultBuffer, new RoundRobinWriteHandler(channel, this.database));
           return;
       }else if(protocolType == ProtocolType.QUERY_PAGE && version == 1){
       }else if(protocolType == ProtocolType.INCREASE && version == 1){
           //读取到数据流
           RoundRobinIncreaseProtocolV1 protocol = new RoundRobinIncreaseProtocolV1();
           protocol.convert(attachment);
           RoundRobinConnection connection = null;
           try {
               //进行合并视图操作
               connection = this.database.open();
               long i = connection.increase(protocol.getTableName(), protocol.getColumn(), protocol.getPos(), protocol.getValue());
               connection.close();
               protocol.setNewValue(i);
           } catch (IOException e) {
               e.printStackTrace();
           } finally {
               try {
                   connection.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
           protocol.setMessageType(MessageType.RESPONSE);
           //写应答数据
           ByteBuffer resultBuffer = protocol.convert();
           resultBuffer.flip();
           //注册异步写入返回信息
           channel.write(resultBuffer, resultBuffer, new RoundRobinWriteHandler(channel, this.database));
           return;
       }else if(protocolType == ProtocolType.CREATE_TABLE && version == 1){
           //读取到数据流
           RoundRobinCreateTableProtocolV1 protocol = new RoundRobinCreateTableProtocolV1();
           protocol.convert(attachment);
           try {
               //进行合并视图操作
               RoundRobinConnection connection = this.database.open();
               connection.createTable(protocol.getTableName(), protocol.getColumns());
               connection.close();
           } catch (IOException e) {
               e.printStackTrace();
           } finally {
           }
           //写应答数据
           ByteBuffer resultBuffer = protocol.convert();
           resultBuffer.flip();
           //注册异步写入返回信息
           channel.write(resultBuffer, resultBuffer, new RoundRobinWriteHandler(channel, this.database));
           return;
       }else if(protocolType == ProtocolType.EXPAND && version == 1){
           //读取到数据流
           RoundRobinExpandProtocolV1 protocol = new RoundRobinExpandProtocolV1();
           protocol.convert(attachment);
           try {
               //进行合并视图操作
               RoundRobinConnection connection = this.database.open();
               Table table = connection.expand(protocol.getTableName(), protocol.getColumns());
               connection.close();
               protocol.setColumns(table.getMetadata().getColumns());
               protocol.setTableName(table.getMetadata().getName());
           } catch (IOException e) {
               e.printStackTrace();
           } finally {
           }
           protocol.setMessageType(MessageType.RESPONSE);
           //写应答数据
           ByteBuffer resultBuffer = protocol.convert();
           resultBuffer.flip();
           //注册异步写入返回信息
           channel.write(resultBuffer, resultBuffer, new RoundRobinWriteHandler(channel, this.database));
           return;
       }else if(protocolType == ProtocolType.MERGE && version == 1){
           //读取到数据流，构建格式对象
           RoundRobinMergeProtocolV1 protocol = new RoundRobinMergeProtocolV1();
           //通过格式对象，构建视图切片对象
           protocol.convert(attachment);
           RoundRobinView view = new RoundRobinView(protocol.getColumns(), protocol.getPos(), protocol.getData());
           RoundRobinConnection connection = null;
           try {
               //使用数据库本地数据库打开连接
               connection = this.database.open();
               //进行合并视图操作
               RoundRobinView newView = connection.merge(protocol.getTableName(), protocol.getMergeType(), view);
               protocol.setColumns(newView.getMetadata().getColumns());
               protocol.setPos(newView.getTime());
               protocol.setData(newView.getData());
               connection.persistent(FormatType.CSV, 1);
               connection.close();
           } catch (IOException e) {
               e.printStackTrace();
           }finally {
           }
           protocol.setMessageType(MessageType.RESPONSE);
           //写应答数据
           ByteBuffer resultBuffer = protocol.convert();
           resultBuffer.flip();
           //注册异步写入返回信息
           channel.write(resultBuffer, resultBuffer, new RoundRobinWriteHandler(channel, this.database));
       }else{
           System.out.println("未知命令");
           return;
       }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}
