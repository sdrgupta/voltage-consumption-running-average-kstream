package com.onlyagricare.topology;

import com.onlyagricare.Utils;
import com.onlyagricare.avro.CountAndSum;
import com.onlyagricare.avro.PowerConsumptionMeasures;
import com.onlyagricare.avro.VoltageMetrics;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import static com.onlyagricare.Constants.COUNT_SUM_AGGREGATOR;
import static com.onlyagricare.config.SerdeConstants.*;

public interface RunningVoltageTopologyBuilder {

    Topology buildTopology(String inputTopic, String outputTopic);

    default Topology buildTopology(
            String inputTopic,
            String outputTopic,
            String outKey,
            TimeWindows hoppingWindow
    ) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, PowerConsumptionMeasures> measuresKStream = streamsBuilder.stream(inputTopic,
                Consumed.with(STRING_SERDE, POWER_CONSUMPTION_MEASURES_AVRO_SERDE));

        measuresKStream
                .peek(Utils::logKV)
                .groupByKey()
                .windowedBy(hoppingWindow)
                .aggregate(
                        () -> new CountAndSum(0L, 0.0),
                        COUNT_SUM_AGGREGATOR,
                        Materialized.with(Serdes.String(), COUNT_AND_SUM_AVRO_SERDE)
                )
                .mapValues(Utils::toRoundedAverage)
                .toStream()
                .map((window, averageVoltage) -> toVoltageMetrics(window, averageVoltage, outKey))
                .peek(Utils::logKV)
                .to(outputTopic, Produced.with(STRING_SERDE, VOLTAGE_METRICS_AVRO_SERDE));

        return streamsBuilder.build();
    }

    private KeyValue<String, VoltageMetrics> toVoltageMetrics(Windowed<String> window, Double dailyAverageVoltage, String key) {
        VOLTAGE_METRICS_BUILDER.setWindowStart(window.window().start());
        VOLTAGE_METRICS_BUILDER.setWindowEnd(window.window().end());
        VOLTAGE_METRICS_BUILDER.setAverageVoltage(dailyAverageVoltage);
        return new KeyValue<>(key, VOLTAGE_METRICS_BUILDER.build());
    }

    VoltageMetrics.Builder VOLTAGE_METRICS_BUILDER = VoltageMetrics.newBuilder();
}
