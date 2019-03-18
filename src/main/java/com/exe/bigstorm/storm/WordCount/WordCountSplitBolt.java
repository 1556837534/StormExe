package com.exe.bigstorm.storm.WordCount;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * @BelongsProject: storm
 * @BelongsPackage: com.exe.bigstorm.storm.WordCount
 * @Author: Jackson_J
 * @CreateTime: 2019-03-18 15:08
 * @Description: 单词计数Storm -- Bolt 程序负责拆分数据 该程序有多个实例 因为它作为任务可能分配到不同的 supervisor 子节点中
 */
public class WordCountSplitBolt extends BaseRichBolt {
    private OutputCollector collector;

    // OutputCollector 初始化该Bolt的输出流
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
       this.collector = outputCollector;
    }

    // 从上一个组件接收的数据 如何在当前组件的处理逻辑
    @Override
    public void execute(Tuple tuple) {
         // 获取上一个组件输出的Tuple格式
        String sentence = tuple.getStringByField("sentence");
        // 分词操作
        String[] words = sentence.split(" ");
        //输出
        for (String word : words) {
            // 输出的格式需要遵循 declareOutputFields 定义的格式
            this.collector.emit(new Values(word,1));
        }
    }

    // 定义该组件刷出的Tuple 格式
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("word","number"));
    }
}
