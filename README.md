# logback+kafka+logstash+elasticsearch+kibana日志平台Java端代码

日志的分析和监控在系统开发中占非常重要的地位，系统越复杂，日志的分析和监控就越重要，常见的需求有:

* 根据关键字查询日志详情
* 监控系统的运行状况
* 统计分析，比如接口的调用次数、执行时间、成功率等
* 异常数据自动触发消息通知
* 基于日志的数据挖掘

很多团队在日志方面可能遇到的一些问题有:
* 开发人员不能登录线上服务器查看详细日志，经过运维周转费时费力
* 日志数据分散在多个系统，难以查找
* 日志数据量大，查询速度慢
* 一个调用会涉及多个系统，难以在这些系统的日志中快速定位数据
* 数据不够实时

整个日志平台的工作流程：
* logback拦截日志，组装日志，并将日志发送到kafka集群。
* logstash接收kafka日志数据，发送到elasticsearch集群。
* kibana获取到ela中的日志，可以通过kibana的UI界面检索日志。

本工程实现的是上述步骤的流程1，实现了应用端快速接入方案，只需要进行简单的配置，即可实现将运行中的产生的日志输送到kafka.
其他流程相关软件已经实现。
# 接入说明

## 引入依赖
```xml
<dependency>
    <groupId>com.log</groupId>
    <artifactId>log-springboot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```
## 配置
```java
package com.demo.log.config;

import com.log.kafka.KafkaConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class DemoKafkaConfig extends KafkaConfig {

    //kafka参数配置，比如连接地址，超时时间等，参考org.apache.kafka.clients.producer.ProducerConfig类
    //默认的参数请查看KafkaConfig.defaultConfig()
    @Override
    public Map<String, Object> config() {
            Map<String, Object> kafkaCfg = new HashMap<>();
    
            kafkaCfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092,localhost:9093");
    
            return kafkaCfg;
        }

    //应用的名称
    @Override
    public String appName() {
        return "DemoApplication";
    }
    //日志消息的topic，需要和logstash中对应
    @Override
    public String topic() {
        return "application-log";
    }
}
```
启动类上添加注解@EnableKafkaLog
```java
@EnableKafkaLog
@SpringBootApplication
public class LogDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogDemoApplication.class, args);
        new MyThread(){}.start();
    }

}
```
## logstash配置
需要注意topics需要和java代码中配置的一样。
如果需要配置多个，则如下配置
topics => ["application-log","application-log-1","application-log-2"]
更多配置参考[官方文档](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-kafka.html)
```xml
input {
    
    kafka {
	    bootstrap_servers => "localhost:9092,localhost:9093"
	    topics => ["application-log"]
        type => "kafka-log"
        group_id => "consumer-test"
        auto_offset_reset => "latest"
        codec => json
   
    }


}

filter {

}

output {

   if[type] == "kafka-log" {
	   elasticsearch {
	       index => "index-log-%{+YYYY-MM-dd}"
	       hosts => ["127.0.0.1:9200"]
	       document_type => "kafka"
        }
    }

    stdout {
        codec => json_lines
    }
}

```