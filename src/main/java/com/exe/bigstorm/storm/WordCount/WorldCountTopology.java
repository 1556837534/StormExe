package com.exe.bigstorm.storm.WordCount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * @BelongsProject: storm
 * @BelongsPackage: com.exe.bigstorm.storm.WordCount
 * @Author: Jackson_J
 * @CreateTime: 2019-03-18 15:12
 * @Description: 单词计数Storm -- 一个Topology主程序 负责管理spout与 bolt 任务
 *
 * 1023-Storm 单词计数流向与demo
 */
public class WorldCountTopology {
    public static void main(String[] args) throws Exception {
        // 主程序
        TopologyBuilder builder = new TopologyBuilder();

        // 设置 任务的Spout 采集数据
        // 名称
        builder.setSpout("mywordCount_spout",new WorldCountSpout());

        // 指定 Spout 输出数据到 第一个 Bolt 中的 数据分组策略
        // 设置任务的第一个 Bolt 组件 shuffleGrouping（）中的参数是上一个组件的id  随机分组
        builder.setBolt("mywordCount_split",new WordCountSplitBolt()).shuffleGrouping("mywordCount_spout");

        //设置第二个Bolt 组件 指定 bolt 输出数据到 第一个 Bolt 中的 数据分组策略  fieldsGrouping （上一级组件，分组的字段【必须是当前组件输出的Tuple中的字段】）按字段分组
        builder.setBolt("myworldCount_total",new WorldCountTotalBolt()).fieldsGrouping("mywordCount_split",new Fields("word"));

        // 设置任务第三个 bolt 组件 将结果保存到Redis 分组策略 随机分组
        builder.setBolt("mywordCount_redisBolt",createRedisBolt()).shuffleGrouping("myworldCount_total");

        // 创建一个 topology 任务
        StormTopology wc = builder.createTopology();

        //配置参数
        Config config = new Config();

        // 执行一个Storm 任务有两种方式
        //1. 本地模式 : 不需要提交到Storm 集群上  用在开发的时候
          //LocalCluster localCluster = new LocalCluster();
        // submitTopology（任务名称,配置信息, 任务）
         //localCluster.submitTopology("myWordCountDemo",config,wc);
        // 2 集群模式: 打包成Jar 包 提交到 Storm 集群运行
        // 运行命令  storm jar storm-0.0.1-SNAPSHOT.jar com.exe.bigstorm.storm.WordCount.WorldCountTopology mydemo
        StormSubmitter.submitTopology("myWordCountDemo",config,wc);
    }

    private static IRichBolt createRedisBolt(){
        // 返回一个Redis 的 bolt 将结果保存到Redis 中
        return null;
    }
}
