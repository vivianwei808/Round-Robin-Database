package org.wing4j.rrd.core.engine;

import org.wing4j.rrd.*;
import org.wing4j.rrd.core.Table;
import org.wing4j.rrd.core.TableMetadata;
import org.wing4j.rrd.net.connector.RoundRobinConnector;

import java.io.IOException;

/**
 * Created by wing4j on 2017/8/4.
 */
public class RemoteTable implements Table {
    String tableName;
    RoundRobinConnector connector;

    public RemoteTable(String tableName, RoundRobinConnector connector) {
        this.tableName = tableName;
        this.connector = connector;
    }

    @Override
    public TableMetadata getMetadata() {
        try {
            return connector.getTableMetadata(tableName);
        } catch (Exception e) {
            throw new RoundRobinRuntimeException("", e);
        }
    }

    @Override
    public Table lock() {
        return null;
    }

    @Override
    public Table unlock() {
        return null;
    }

    @Override
    public long increase(String column) {
        try {
            return connector.increase(tableName, column, 1);
        } catch (Exception e) {
            throw new RoundRobinRuntimeException("", e);
        }
    }

    @Override
    public long increase(String column, int val) {
        try {
            return connector.increase(tableName, column, val);
        } catch (Exception e) {
            throw new RoundRobinRuntimeException("", e);
        }
    }

    @Override
    public int getSize() {
        try {
            return connector.getDataSize(tableName);
        } catch (Exception e) {
            throw new RoundRobinRuntimeException("", e);
        }
    }

    @Override
    public long set(int time, String column, long val) {
        return 0;
    }

    @Override
    public long get(int time, String column) {
        return 0;
    }

    @Override
    public RoundRobinView slice(int size, String... columns) {
        try {
            return connector.read(size, tableName, columns);
        } catch (Exception e) {
            throw new RoundRobinRuntimeException("", e);
        }
    }

    @Override
    public RoundRobinView slice(int size, int time, String... columns) {
        try {
            return connector.read(time % getSize(), size, tableName, columns);
        } catch (Exception e) {
            throw new RoundRobinRuntimeException("", e);
        }
    }

    @Override
    public Table expand(String... columns) {
        try {
            connector.expand(tableName, columns);
        } catch (Exception e) {

        }
        return this;
    }

    @Override
    public Table merge(RoundRobinView view, int time, MergeType mergeType) {
        try {
            connector.merge(tableName, time, view, mergeType);
        } catch (Exception e) {

        }
        return this;
    }

    @Override
    public Table merge(RoundRobinView view, MergeType mergeType) {
        try {
            connector.merge(tableName, view, mergeType);
        } catch (Exception e) {

        }
        return this;
    }

    @Override
    public Table persistent(FormatType formatType, int version) throws IOException {
        System.out.println("尉氏县");
        return this;
    }

    @Override
    public Table persistent() throws IOException {
        System.out.println("尉氏县");
        return this;
    }

    @Override
    public void drop() throws IOException {
        try {
            connector.dropTable(tableName);
        } catch (Exception e) {

        }
    }

    @Override
    public Table registerTrigger(RoundRobinTrigger trigger) {
        return null;
    }
}