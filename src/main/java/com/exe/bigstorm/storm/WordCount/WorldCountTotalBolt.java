package com.exe.bigstorm.storm.WordCount;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: storm
 * @BelongsPackage: com.exe.bigstorm.storm.WordCount
 * @Author: Jackson_J
 * @CreateTime: 2019-03-18 15:11  作为最后一个Bolt组件
 * @Description: 单词计数Storm -- Bolt 程序负责单词计数 该程序有多个实例 因为它作为任务可能分配到不同的 supervisor 子节点中
 */
public class WorldCountTotalBolt extends BaseRichBolt {
    private OutputCollector collector;

    // 定义一个Map 集合 保存最后的结果
    private Map<String,Integer> map = new HashMap<>();

    // OutputCollector 初始化该Bolt的输出流
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
         this.collector = outputCollector;
    }

    // 从上一个组件接收的数据 如何在当前组件的处理逻辑
    @Override
    public void execute(Tuple tuple) {
        // 获取上一个组件输出的Tuple格式
        String word = tuple.getStringByField("word");
        Integer count = tuple.getIntegerByField("number");

        // 计算总数  这里可以输出到下个组件 或者直接输出 这里我选择直接输出
        if (map.containsKey(word)) {
            // 结果集中存在该单词 进行累加
            int total = map.get(word);
            map.put(word,total+count);
        } else {
            map.put(word,count);
        }

        // 直接输出到屏幕
        System.out.println("输出的结果是："+map);

        // 将处理的结果 继续发给下个组件
        this.collector.emit(new Values(word,map.get(word)));
    }

    // 定义该组件刷出的Tuple 格式
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
          // 输出格式  Beijing 3
        outputFieldsDeclarer.declare(new Fields("word","total"));
    }
}
