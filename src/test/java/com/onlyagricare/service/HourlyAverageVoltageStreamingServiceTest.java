package com.onlyagricare.service;

import com.onlyagricare.Utils;
import com.onlyagricare.avro.PowerConsumptionMeasures;
import com.onlyagricare.avro.VoltageMetrics;
import com.onlyagricare.common.TestUtils;
import com.onlyagricare.topology.HourlyAverageVoltageTopologyBuilder;
import com.onlyagricare.topology.RunningVoltageTopologyBuilder;
import org.apache.kafka.streams.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static com.onlyagricare.config.SerdeConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HourlyAverageVoltageStreamingServiceTest {

    private final String inputTopicName = "input";
    private final String outputTopicName = "output";
    private TopologyTestDriver testDriver;

    @Before
    public void setup() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregate-test");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 1024 * 256);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100000);
        properties.put("schema.registry.url", "mock://windowed-aggregation-test");
        RunningVoltageTopologyBuilder topologyBuilder = new HourlyAverageVoltageTopologyBuilder();
        Topology topology = topologyBuilder.buildTopology(inputTopicName, outputTopicName);
        testDriver = new TopologyTestDriver(topology, properties);
    }

    @After
    public void teardown() {
        testDriver.close();
    }

    @Test
    public void validateIfTestDriverCreated() {
        assertNotNull(testDriver);
    }

    @Test
    public void testTopology() {
        final TestInputTopic<String, PowerConsumptionMeasures> inputTopic =
                testDriver.createInputTopic(inputTopicName,
                        STRING_SERDE.serializer(),
                        POWER_CONSUMPTION_MEASURES_AVRO_SERDE.serializer());

        final TestOutputTopic<String, VoltageMetrics> outputTopic =
                testDriver.createOutputTopic(outputTopicName,
                        STRING_SERDE.deserializer(),
                        VOLTAGE_METRICS_AVRO_SERDE.deserializer());

        List<PowerConsumptionMeasures> powerConsumptionMeasures = TestUtils
                .getTestData("/datasets/power_consumption_test_data.txt");

        // Push the data to input topic
        powerConsumptionMeasures.forEach(measure -> {
            long epochMillis = Utils.toEpochMillis(measure.getDate() + "-" + measure.getTime());
            inputTopic.pipeInput("TEST_VOLTAGE_METRICS", measure, epochMillis);
        });

        List<VoltageMetrics> results = outputTopic.readValuesToList();
        Assert.assertEquals(12, results.size());

        long expectedAverage = Double.doubleToLongBits(232.175);
        long actualAverage = Double.doubleToLongBits(results.get(7).getAverageVoltage());
        assertEquals(expectedAverage, actualAverage);

        expectedAverage = Double.doubleToLongBits(232.713);
        actualAverage = Double.doubleToLongBits(results.get(9).getAverageVoltage());
        assertEquals(expectedAverage, actualAverage);
    }

}
