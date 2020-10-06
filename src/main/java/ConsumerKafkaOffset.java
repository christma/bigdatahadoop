import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerKafkaOffset {

    public static final String brokerList = "hadoop000:9092";
    public static final String topic = "helloworld";
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
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        TopicPartition tp = new TopicPartition(topic, 0);
        consumer.assign(Arrays.asList(tp));

        long lastConsumedOffset = -1;

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);

                if (records.isEmpty()) {
                    break;
                }
                List<ConsumerRecord<String, String>> partitionRecords = records.records(tp);
                lastConsumedOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                consumer.commitAsync();
            }
            System.out.println("consumed offset is " + lastConsumedOffset);
            OffsetAndMetadata offsetAndMetadata = consumer.committed(tp);
            System.out.println("commited offset is " + offsetAndMetadata.offset());
            long potition = consumer.position(tp);
            System.out.println("the offset of the next record is " + potition);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }


}
