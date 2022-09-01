package com.onlyagricare.config;

import com.onlyagricare.Utils;
import com.onlyagricare.avro.CountAndSum;
import com.onlyagricare.avro.PowerConsumptionMeasures;
import com.onlyagricare.avro.VoltageMetrics;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Map;

import static com.onlyagricare.config.AppConfig.PROPERTIES;

public class SerdeConstants {

    private SerdeConstants() {
        // Preventing Instantiation
    }

    private static final Map<String, Object> CONFIG_MAP = Utils.propertiesToMap(PROPERTIES);

    public static final Serde<String> STRING_SERDE = Serdes.String();

//    public static final Serde<Double> DOUBLE_SERDE = Serdes.Double();

    public static final SpecificAvroSerde<PowerConsumptionMeasures> POWER_CONSUMPTION_MEASURES_AVRO_SERDE =
            Utils.getSpecificAvroSerde(CONFIG_MAP);

    public static final SpecificAvroSerde<VoltageMetrics> VOLTAGE_METRICS_AVRO_SERDE =
            Utils.getSpecificAvroSerde(CONFIG_MAP);

    public static final SpecificAvroSerde<CountAndSum> COUNT_AND_SUM_AVRO_SERDE =
            Utils.getSpecificAvroSerde(CONFIG_MAP);


}
