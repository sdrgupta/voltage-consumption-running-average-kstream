package com.onlyagricare;

import com.onlyagricare.avro.CountAndSum;
import com.onlyagricare.avro.PowerConsumptionMeasures;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.onlyagricare.Constants.POWER_CONSUMPTION_MEASURES_BUILDER;
import static com.onlyagricare.Constants.SDF;
import static com.onlyagricare.config.AppConfig.PROPERTIES;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static Properties loadConfig(final String configFile) {
        final Properties cfg = new Properties();
        loadFrom(configFile, cfg);
        return cfg;
    }

    public static void loadFrom(final String configFile, Properties properties) {
        try (InputStream inputStream = Utils.class.getResourceAsStream(configFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NewTopic newTopic(final String topicName, int nPartitions, short replicationFactor) {
        return new NewTopic(topicName, nPartitions, replicationFactor);
    }

    public static Callback callback(Logger callerLog) {
        return (metadata, exception) -> {
            if (exception != null) {
                callerLog.error("Producing records encountered error.", exception);
            } else {
                callerLog.info("Record produced at offset = {}, timestamp = {}", metadata.offset(), metadata.timestamp());
            }
        };
    }

    public static double parseToDouble(String valueStr, double defaultValue) {
        String globalActivePowerStr = valueStr.trim();
        return globalActivePowerStr.isEmpty() ? defaultValue : Double.parseDouble(globalActivePowerStr);
    }

    public static Map<String, Object> propertiesToMap(final Properties properties) {
        final Map<String, Object> configs = new HashMap<>();
        properties.forEach((key, value) -> configs.put((String) key, value));
        return configs;
    }

    public static Properties copyFrom(Properties properties) {
        Properties copy = new Properties();
        properties.forEach((key, value) -> copy.setProperty((String) key, (String) value));
        return copy;
    }

    public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Map<String, Object> serdeConfig) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        specificAvroSerde.configure(serdeConfig, false);
        return specificAvroSerde;
    }

    public static long toEpochMillis(String dateTime) {
        try {
            return SDF.parse(dateTime).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static PowerConsumptionMeasures buildPowerConsumptionMeasures(String line) {
        String[] measures = line.split(";");
        String date = measures[0].trim();
        String time = measures[1].trim();
        double globalActivePower = parseToDouble(measures[2], 0.0);
        double globalReactivePower = parseToDouble(measures[3], 0.0);
        double voltage = parseToDouble(measures[4], 0.0);
        double globalIntensity = parseToDouble(measures[5], 0.0);
        double subMetering1 = parseToDouble(measures[6], 0.0);
        double subMetering2 = parseToDouble(measures[7], 0.0);
        double subMetering3 = parseToDouble(measures[8], 0.0);

        POWER_CONSUMPTION_MEASURES_BUILDER.setDate(date);
        POWER_CONSUMPTION_MEASURES_BUILDER.setTime(time);
        POWER_CONSUMPTION_MEASURES_BUILDER.setGlobalActivePower(globalActivePower);
        POWER_CONSUMPTION_MEASURES_BUILDER.setGlobalReactivePower(globalReactivePower);
        POWER_CONSUMPTION_MEASURES_BUILDER.setGlobalIntensity(globalIntensity);
        POWER_CONSUMPTION_MEASURES_BUILDER.setVoltage(voltage);
        POWER_CONSUMPTION_MEASURES_BUILDER.setSubMetering1(subMetering1);
        POWER_CONSUMPTION_MEASURES_BUILDER.setSubMetering2(subMetering2);
        POWER_CONSUMPTION_MEASURES_BUILDER.setSubMetering3(subMetering3);

        return POWER_CONSUMPTION_MEASURES_BUILDER.build();
    }

    public static void logKV(String key, Object value) {
        LOG.info("Key = {},  Value = {}", key, value);
    }

    public static void createTopics(String... topics) {
        final Map<String, Object> configMap = Utils.propertiesToMap(PROPERTIES);
        final int nPartitions = Integer.parseInt(configMap.get("partitions").toString());
        final short replicationFactor = Short.parseShort(configMap.get("replication.factor").toString());
        List<NewTopic> newTopics = Arrays.stream(topics)
                .map(topic -> Utils.newTopic(topic, nPartitions, replicationFactor))
                .collect(Collectors.toList());
        try (Admin admin = Admin.create(configMap)) {
            admin.createTopics(newTopics);
        }
    }

    public static double toRoundedAverage(CountAndSum countAndSum) {
        long count = countAndSum.getCount();
        double sum = countAndSum.getSum();
        double average = sum / count;
        return Math.round(average * 1000.0) / 1000.0;
    }

    public static String getResourcePath(String sourceDataSet) {
        return Objects.requireNonNull(Utils.class.getClassLoader().getResource(sourceDataSet)).getPath();
    }

    public static InputStream getResourceInputStream(String sourceDataSet) {
        return Utils.class.getResourceAsStream(sourceDataSet);
    }
}
