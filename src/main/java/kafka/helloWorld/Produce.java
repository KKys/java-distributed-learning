package kafka.helloWorld;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;
import java.util.Properties;



/**
 * kafka生产者
 * Created by yaosheng on 2017/2/9.
 */

public class Produce {

    public static void main(String[] args) {
        System.out.println("begin produce");
        connectionKafka();
        System.out.println("finish produce");
    }

    public static void connectionKafka() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "121.42.8.85:9092");
        //ack是判别请求是否为完整的条件（就是是判断是不是成功发送了）。我们指定了“all”将会阻塞消息，这种设置性能最低，但是是最可靠的。
        //props.put("acks", "all");
        //失败后重试次数
        props.put("retries", 0);
        //默认缓冲可立即发送，即遍缓冲空间还没有满，但是，如果你想减少请求的数量，可以设置linger.ms大于0，就把消息先加到缓存队列中，稍后一同发出
        //props.put("linger.ms", 1);
        //缓存未发送消息的大小
        props.put("batch.size", 16384);
        //控制生产者可用的缓存总量
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord<String, String>("test2", Integer.toString(i), Integer.toString(i)));
            System.out.println("send成功");
            try {
                //两秒发一次
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        producer.close();
    }
}
