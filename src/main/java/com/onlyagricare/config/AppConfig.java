package com.onlyagricare.config;

import com.onlyagricare.Utils;

import java.util.Properties;

public class AppConfig {

    public static final Properties PROPERTIES =
            Utils.loadConfig("/application.properties");

//    public static final String APPLICATION_ID = PROPERTIES.getProperty("application.id");
}
