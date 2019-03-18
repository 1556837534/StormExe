package com.exe.bigstorm.storm.WordCount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.Map;

/**
 * @BelongsProject: storm
 * @BelongsPackage: com.exe.bigstorm.storm.WordCount
 * @Author: Jackson_J
 * @CreateTime: 2019-03-18 21:09
 * @Description: Storm 与 HBase 集成
 * 从上一级的 bolt组件中接收数据 并保存 到Hbase 中
 * // 首先 在HBase 中创建一张表
 *
 */
public class WordCountHBaseBolt extends BaseRichBolt {
    // 定于Hbase 表的客户端
    private HTable hTable;
    private Configuration configuration;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        // 准备 进行一些初始化的工作
        // 得到 Hbase 的客户端
        try {
            // 需要通过 zookeeper 来操作 HBase
            this.configuration = new Configuration();
            //1. 配置 zookeeper 地址信息
            this.configuration.set("hbase.zookeeper.quorum","192.168.199.135");
            //2.1 得到表的客户端
            this.hTable = new HTable(configuration,"result");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        // 从上一个组件接收数据并插入到HBase 中
        String word = tuple.getStringByField("word");
        String num = String.valueOf(tuple.getIntegerByField("total"));
        // 构造一个 put对象
        Put put = new Put(Bytes.toBytes(word)); // rowkey 行键
        put.add(Bytes.toBytes("info"),Bytes.toBytes("word"),Bytes.toBytes(word));
        put.add(Bytes.toBytes("info"),Bytes.toBytes("total"),Bytes.toBytes(num));
        //插入数据
        try {
            this.hTable.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
