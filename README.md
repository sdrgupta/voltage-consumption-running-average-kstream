# voltage-consumption-running-average-kafka-streams
This app contains three services.
1. **Producer service**: Uses the household power consumption [kaggle](https://www.kaggle.com/datasets/uciml/electric-power-consumption-data-set) dataset. The producer service reads this dataset as a text file and pushes the data to a kafka topic named `household_power_consumption_data`. Each record is pushed to kafka running in confluent cloud as an explicitely timestamped event.
2. **Daily Average Voltage Streaming Service**: This is a kafka streaming consumer that consumes the data from the topic `household_power_consumption_data` and produces a running average of the `voltage` value windowed by daily and pushes the results to the output topic named `household_power_consumption_voltage_daily`.
3. **Hourly Average Voltage Streaming Service**: This is also kafka streaming consumer that consumes the data from the topic `household_power_consumption_data` and produces a running average of the `voltage` value windowed by hourly and pushes the results to the output topic named `household_power_consumption_voltage_hourly`.

```NOTE``` **For more information look at the design diagram.**

## Design Diagram
![Design Diagram](https://github.com/sdrgupta/voltage-consumption-running-average-kstream/blob/main/running_average_voltage_design_diagram.png)

All the topics are associated with the avro schema stored in the confluent schema registry.

## Local Development Environment Setup
Follow below steps to setup local development environment.
1. Checkout source code in a directory of your choice using the following command.
    ```shell
    git clone https://github.com/krishna8git/voltage-consumption-running-average-kafka-streams.git 
    ```
2. Run ```gradlew clean build``` so that the avro based source files are generated. This can be done after importing the project as well.
3. Import as a gradle project in any IDE of your choice.

## Prerequisites to run this application.
1. Confluent Cloud account is required.
2. Create API key and Secret for the Java Client.
3. Rename ```application.properties.template``` to ```application.properties``` and update the secrets.
4. Create Confluent Schema Registry Key and Secret and update them in ```application.properties``` as the code pushes the schemas directly to schema registry if they are not available.

## Running the application
```NOTE```: Before running the application make sure all the prerequisites are met.
1. **Running the Jar**
   ```shell 
   $ ./gradlew clean build
   $ java -jar build/libs/<jarname.jar> <dataset_size>
   ```
2. **Running with docker**
   ```shell
    $ docker build -t <image_name> .
    $ docker run <image_name> <dataset_size>
   ```
3. **Running locally from the IDE**: Run ```Main.java``` file from your IDE with the runtime argument as one of the value from dataset_size

## Contact
Please feel free to raise if you find any issue.
