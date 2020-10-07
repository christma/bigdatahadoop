import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerKafkaThread {

    public static final String brokerList = "hadoop000:9092";
    public static final String topic = "her";
    public static final String groupId = "group.her";
    public static final AtomicBoolean isRunning = new AtomicBoolean(true);

    public static Properties initConfig() {
        Properties prop = new Properties();
//        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        prop.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        prop.put(ConsumerConfig.CLIENT_ID_CONFIG,)
        return prop;
    }

    public static void main(String[] args) {

        Properties properties = initConfig();

        int consumerThreadNum = 4;
        for (int i = 0; i < consumerThreadNum; i++) {

            new KafkaConsumerTherad(properties, topic, i).start();
        }

    }


    private static class KafkaConsumerTherad extends Thread {
        private KafkaConsumer<String, String> kafkaConsumer;
        private String name;

        public KafkaConsumerTherad(Properties properties, String topic, int i) {
            this.kafkaConsumer = new KafkaConsumer<String, String>(properties);
            this.name = "kafka_thread_" + i;
            this.kafkaConsumer.subscribe(Arrays.asList(topic));
        }

        @Override
        public void run() {
            try {
                while (isRunning.get()) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println(name);
                        System.out.println("topic = " + record.topic() + " partition = " + record.partition() + " offset= " + record.offset());
                        System.out.println("key = " + record.key() + " value = " + record.value());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                kafkaConsumer.close();
            }

        }
    }
}
