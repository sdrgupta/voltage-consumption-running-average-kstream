package com.onlyagricare.topology;

import org.apache.kafka.streams.Topology;

import static com.onlyagricare.Constants.ONE_DAY_HOPPING_WINDOW;

public class DailyAverageVoltageTopologyBuilder implements RunningVoltageTopologyBuilder {

    @Override
    public Topology buildTopology(String inputTopic, String outputTopic) {
        return buildTopology(
                inputTopic,
                outputTopic,
                "DAILY_VOLTAGE",
                ONE_DAY_HOPPING_WINDOW
        );
    }
}
