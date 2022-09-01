package com.onlyagricare.service;

import com.onlyagricare.Utils;
import com.onlyagricare.avro.PowerConsumptionMeasures;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

import static com.onlyagricare.config.AppConfig.PROPERTIES;

public class PowerConsumptionDataProducer implements StreamingService {

    private static final Logger LOG = LoggerFactory.getLogger(PowerConsumptionDataProducer.class);
    private static final Map<String, Object> configMap = Utils.propertiesToMap(PROPERTIES);
    private static final String KEY = "HOUSEHOLD_POWER_CONSUMPTION_MEASURES";
    private final String sourceDataSet;
    private final String topic;

    public PowerConsumptionDataProducer(String sourceDataSet, String topic) {
        this.sourceDataSet = sourceDataSet;
        this.topic = topic;
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    }

    @Override
    public void start() {
        final int TOTAL_MEASURES = 9;
        try (Producer<String, PowerConsumptionMeasures> producer = new KafkaProducer<>(configMap)) {
//            String sourceDataSetPath = Utils.getResourcePath(sourceDataSet);
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(
                                 Objects.requireNonNull(this.getClass().getResourceAsStream(sourceDataSet)))
                         )) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.split(";").length == TOTAL_MEASURES) {
                        producer.send(toPublisherRecord(line), Utils.callback(LOG));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                producer.flush();
            }

//            try (Stream<String> measuresStream = Files.lines(Paths.get(sourceDataSetPath))) {
//                LOG.info("Power consumption data publisher started.");
//                measuresStream
//                        .filter(line -> line.split(";").length == TOTAL_MEASURES) // Ignore the invalid records
//                        .map(this::toPublisherRecord)
//                        .forEach(record -> producer.send(record, Utils.callback(LOG)));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } finally {
//                producer.flush();
//            }
        }
    }

    @Override
    public Topology topology() {
        // Since this is the producer no topology required.
        return null;
    }

    private ProducerRecord<String, PowerConsumptionMeasures> toPublisherRecord(String line) {
        PowerConsumptionMeasures measuresObj = Utils.buildPowerConsumptionMeasures(line);
        final long recordTimestamp = Utils.toEpochMillis(measuresObj.getDate() + "-" + measuresObj.getTime());
        return new ProducerRecord<>(topic, 0, recordTimestamp, KEY, measuresObj);
    }

    public static void main(String[] args) {
        final String powerConsumptionDataTopic = PROPERTIES.getProperty("topic.input.power_consumption");
        Utils.createTopics(powerConsumptionDataTopic);
        StreamingService powerConsumptionDataPublisher = new PowerConsumptionDataProducer(
                "/datasets/household_power_consumption_small.txt",
                powerConsumptionDataTopic);
        powerConsumptionDataPublisher.start();
    }
}