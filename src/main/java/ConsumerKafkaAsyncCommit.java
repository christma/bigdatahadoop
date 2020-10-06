import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerKafkaAsyncCommit {

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
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//        prop.put(ConsumerConfig.CLIENT_ID_CONFIG,)
        return prop;
    }

    public static void main(String[] args) {
        Thread.currentThread().setContextClassLoader(null);
        Properties properties = initConfig();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Arrays.asList(topic));
        final int minBatchSize = 10;
        List<ConsumerRecord> buffer = new ArrayList<ConsumerRecord>();
        try {
            while (isRunning.get()) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("topic = " + record.topic() + " partition = " + record.partition() + " offset= " + record.offset());
                    System.out.println("key = " + record.key() + " value = " + record.value());
                    System.out.println(record.offset());
                    OffsetAndMetadata offsetAndMetadata = consumer.committed(new TopicPartition(topic, 0));
                    System.out.println("commited offset is " + offsetAndMetadata.offset());


                    consumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                            if (e == null) {
                                System.out.println("==================> " + record.offset());
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }

        }
    }


}
