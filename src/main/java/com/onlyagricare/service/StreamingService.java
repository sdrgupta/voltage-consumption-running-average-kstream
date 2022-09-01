package com.onlyagricare.service;

import org.apache.kafka.streams.Topology;

public interface StreamingService {
    
    void start();

    Topology topology();

}
