package kafka.helloWorld;

/**
 * Created by yaosheng on 2017/2/9.
 */
public interface KafkaProperties {
    final static String zkConnect = "121.42.8.85:2181,121.42.8.85:2182,121.42.8.85:2183";
    final static String groupId = "group1";
    final static String topic = "topic1";
    final static String kafkaServer = "121.42.8.85:9092,121.42.8.85:9092,121.42.8.85:9092";
    final static int kafkaProducerBufferSize = 64 * 1024;
    final static int connectionTimeOut = 20000;
    final static int reconnectInterval = 10000;
    final static String topic2 = "topic2";
    final static String topic3 = "topic3";
    final static String clientId = "SimpleConsumerDemoClient";
}
