package com.onlyagricare;

import com.onlyagricare.service.DailyAverageVoltageStreamingService;
import com.onlyagricare.service.HourlyAverageVoltageStreamingService;
import com.onlyagricare.service.PowerConsumptionDataProducer;
import com.onlyagricare.service.StreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.onlyagricare.config.AppConfig.PROPERTIES;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final Set<String> DATASET_SIZES = Set.of("small", "medium", "large", "xlarge", "2xlarge");

    private static final String DATASET_PREFIX = "/datasets/household_power_consumption_%s.txt";

    public static void main(String[] args) {
        validateArgs(args);
        final String powerConsumptionDataTopic = PROPERTIES.getProperty("topic.input.power_consumption");
        final String dailyVoltageTopic = PROPERTIES.getProperty("topic.output.daily_voltage");
        final String hourlyVoltageTopic = PROPERTIES.getProperty("topic.output.hourly_voltage");

        Utils.createTopics(
                powerConsumptionDataTopic,
                dailyVoltageTopic,
                hourlyVoltageTopic);


        StreamingService powerConsumptionDataProducer = new PowerConsumptionDataProducer(
                String.format(DATASET_PREFIX, args[0]),
                powerConsumptionDataTopic);

        StreamingService dailyAverageVoltageStreamingService =
                new DailyAverageVoltageStreamingService(powerConsumptionDataTopic, dailyVoltageTopic);

        StreamingService hourlyAverageVoltageStreamingService =
                new HourlyAverageVoltageStreamingService(powerConsumptionDataTopic, hourlyVoltageTopic);

        // start Consumer Streaming first then the Producer
        dailyAverageVoltageStreamingService.start();
        hourlyAverageVoltageStreamingService.start();
        powerConsumptionDataProducer.start();
    }

    private static void validateArgs(String[] args) {
        if (args.length < 1) {
            LOG.error("No arguments specified!");
            LOG.info("Please specify the dataset size to be used!" +
                    "\nAvailable dataset sizes:\n\t1. small\n\t2. medium\n\t3. large\n\t4. xlarge\n\t5. 2xlarge" +
                    "\nUsage Example for running docker image" +
                    "\n\teg. docker run <image_name> medium" +
                    "\nUsage Example for running jar" +
                    "\n\teg. java -jar <jar_name> small"
            );
            System.exit(1);
        }
        if (!DATASET_SIZES.contains(args[0])) {
            LOG.error("Invalid Dataset Size!!" +
                    "\nAvailable dataset sizes:\n\t1. small\n\t2. medium\n\t3. large\n\t4. xlarge\n\t5. 2xlarge");
            System.exit(1);
        }
    }
}
