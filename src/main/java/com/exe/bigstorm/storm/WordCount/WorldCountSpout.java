package com.exe.bigstorm.storm.WordCount;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

/**
 * @BelongsProject: storm
 * @BelongsPackage: com.exe.bigstorm.storm.WordCount
 * @Author: Jackson_J
 * @CreateTime: 2019-03-18 15:08
 * @Description: 单词计数Storm -- Spout 程序负责采集数据
 */
public class WorldCountSpout extends BaseRichSpout {
    //定义变量表示spout 输出流
    private SpoutOutputCollector spoutOutputCollector;

    // 这里数据需要进行外部集成  先模拟产生一些数据
    private String[] data = {"I Love Beijing","I Love China","Beijing is the capital of China"};

    // SpoutOutputCollector : 当 Spout 组件处理完成后 将数据使用这个输出流输出
    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
         //初始化
         this.spoutOutputCollector = spoutOutputCollector;
    }

    // 会由 Storm 框架 进行调用 用于接收外部的数据
    @Override
    public void nextTuple() {
        // 这里为了便于测试观察数据 使用个间隔3s
        Utils.sleep(3000);

         // 这里模拟随机产生字符串
        int random = (new Random()).nextInt(3);
        String sentence = data[random];
        System.out.println("Spout采集的数据:" + sentence);

        // 发送给下个组件进行处理
        this.spoutOutputCollector.emit(new Values(sentence));
    }

    // 声明输出 Tuple的格式(Scheme)
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
         outputFieldsDeclarer.declare(new Fields("sentence"));
    }
}
