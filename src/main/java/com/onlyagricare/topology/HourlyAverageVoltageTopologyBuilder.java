package com.onlyagricare.topology;

import org.apache.kafka.streams.Topology;

import static com.onlyagricare.Constants.ONE_HOUR_HOPPING_WINDOW;

public class HourlyAverageVoltageTopologyBuilder implements RunningVoltageTopologyBuilder {

    @Override
    public Topology buildTopology(String inputTopic, String outputTopic) {
        return buildTopology(
                inputTopic,
                outputTopic,
                "HOURLY_VOLTAGE",
                ONE_HOUR_HOPPING_WINDOW
        );
    }

}
