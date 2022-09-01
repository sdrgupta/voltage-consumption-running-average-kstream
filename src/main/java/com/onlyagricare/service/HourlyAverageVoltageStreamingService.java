package com.onlyagricare.service;

import com.onlyagricare.Utils;
import com.onlyagricare.error.StreamsCustomUncaughtExceptionHandler;
import com.onlyagricare.topology.HourlyAverageVoltageTopologyBuilder;
import com.onlyagricare.topology.RunningVoltageTopologyBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.onlyagricare.config.AppConfig.PROPERTIES;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

public class HourlyAverageVoltageStreamingService implements StreamingService {

    private static final Logger LOG = LoggerFactory.getLogger(HourlyAverageVoltageStreamingService.class);
    private final String inputTopic;
    private final String outputTopic;

    public HourlyAverageVoltageStreamingService(String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
    }

    @Override
    public void start() {
        Properties properties = Utils.copyFrom(PROPERTIES);
        properties.setProperty(APPLICATION_ID_CONFIG, "hourly-average-voltage");
        LOG.info("Kafka Streaming with application id '{}' started.", properties.getProperty(APPLICATION_ID_CONFIG));
        KafkaStreams kafkaStreams = new KafkaStreams(topology(), properties);
        kafkaStreams.setUncaughtExceptionHandler(new StreamsCustomUncaughtExceptionHandler());
        kafkaStreams.start();
        LOG.info("Hourly average voltage streaming publisher started.");
    }

    @Override
    public Topology topology() {
        RunningVoltageTopologyBuilder topologyBuilder = new HourlyAverageVoltageTopologyBuilder();
        return topologyBuilder.buildTopology(inputTopic, outputTopic);
    }

}
