package com.onlyagricare;

import com.onlyagricare.avro.CountAndSum;
import com.onlyagricare.avro.PowerConsumptionMeasures;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.TimeWindows;

import java.text.SimpleDateFormat;
import java.time.Duration;

public class Constants {
    private Constants() {
        // Preventing from instantiation
    }

    public static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
    public static final TimeWindows ONE_DAY_HOPPING_WINDOW = TimeWindows.ofSizeWithNoGrace(Duration.ofDays(1));
    public static final TimeWindows ONE_HOUR_HOPPING_WINDOW = TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1));

    public static final PowerConsumptionMeasures.Builder POWER_CONSUMPTION_MEASURES_BUILDER = PowerConsumptionMeasures.newBuilder();

    public static Aggregator<String, PowerConsumptionMeasures, CountAndSum> COUNT_SUM_AGGREGATOR =
            (key, value, aggregator) -> {
                aggregator.setCount(aggregator.getCount() + 1);
                aggregator.setSum(aggregator.getSum() + value.getVoltage());
                return aggregator;
            };

}
