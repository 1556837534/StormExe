package com.exe.bigstorm.storm.WordCount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.redis.bolt.RedisStoreBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.ITuple;

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
        //builder.setBolt("mywordCount_redisBolt",createRedisBolt()).shuffleGrouping("myworldCount_total");
        // 与HDFS 集成
        //builder.setBolt("mywordCount_hdfsBolt",createHDFSBolt()).shuffleGrouping("myworldCount_total");
        // 与HBase 集成
        builder.setBolt("mywordCount_hdfsBolt",new WordCountHBaseBolt()).shuffleGrouping("myworldCount_total");

        // 创建一个 topology 任务
        StormTopology wc = builder.createTopology();

        //配置参数
        Config config = new Config();

        // 执行一个Storm 任务有两种方式
        //1. 本地模式 : 不需要提交到Storm 集群上  用在开发的时候
          LocalCluster localCluster = new LocalCluster();
        // submitTopology（任务名称,配置信息, 任务）
         localCluster.submitTopology("myWordCountDemo",config,wc);
        // 2 集群模式: 打包成Jar 包 提交到 Storm 集群运行
        // 运行命令  storm jar storm-0.0.1-SNAPSHOT.jar com.exe.bigstorm.storm.WordCount.WorldCountTopology mydemo
        //StormSubmitter.submitTopology("myWordCountDemo",config,wc);
    }

    private static IRichBolt createHDFSBolt() {
        // 创建一个HDFS Bolt 把数据写出到HDFS
        HdfsBolt bolt = new HdfsBolt();
        // 指定 HDFS 地址  就是NameNode 地址
        bolt.withFsUrl("hdfs://192.168.199.135:9000");
        //指定 HDFS 的目录
        bolt.withFileNameFormat(new DefaultFileNameFormat().withPath("/stormdata"));

        // 指定数据的分隔符 默认是制表符 tab键
        bolt.withRecordFormat(new DelimitedRecordFormat().withFieldDelimiter("|"));

        // 由于数据在实时产生 产生到HDFS 时 每5M 生成一个文件
        bolt.withRotationPolicy(new FileSizeRotationPolicy(5.0f, FileSizeRotationPolicy.Units.MB));

        // 指定 HDFS 数据同步时间 隔多长数据与HDFS进行同步 当Tuple 中的结果 到了1K 就进行同步
        bolt.withSyncPolicy(new CountSyncPolicy(100));

        return null;
    }

    private static IRichBolt createRedisBolt(){
        // 需要引入 storm-redis 的jar包 与jedis 包
        // 返回一个Redis 的 bolt 将结果保存到Redis 中
        // 创建redis 连接池 poolConfig
        JedisPoolConfig.Builder builder = new JedisPoolConfig.Builder();
        builder.setHost("192.168.157.112");
        builder.setPort(6379);
        JedisPoolConfig config = builder.build();

        // storeMapper 表示存入Redis 的数据格式


        return new RedisStoreBolt(config, new RedisStoreMapper() {
            @Override
            public RedisDataTypeDescription getDataTypeDescription() {// 指定存入Redis的数据格式是什么?
                // 这里是有Hash 集合
                // 数据类型  变量名称
                return new RedisDataTypeDescription(RedisDataTypeDescription.RedisDataType.HASH,"wordcount");
            }

            @Override
            public String getKeyFromTuple(ITuple iTuple) {// 表示从上一个组件中接收的key
                return iTuple.getStringByField("word");
            }

            @Override
            public String getValueFromTuple(ITuple iTuple) {// 表示从上一个组件中接收的val
                return String.valueOf(iTuple.getIntegerByField("total"));
            }
        });
    }
}
