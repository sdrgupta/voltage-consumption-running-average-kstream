package com.onlyagricare.common;

import com.onlyagricare.Utils;
import com.onlyagricare.avro.PowerConsumptionMeasures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtils {

    public static List<PowerConsumptionMeasures> getTestData(String sourceDataSet) {
        String sourceDataSetPath = Objects.requireNonNull(TestUtils.class.getResource(sourceDataSet)).getFile();
        try (Stream<String> measuresStream = Files.lines(Paths.get(sourceDataSetPath))) {
            return measuresStream
                    .map(Utils::buildPowerConsumptionMeasures)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
