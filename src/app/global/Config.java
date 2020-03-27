package app.global;

import java.io.FileInputStream;
import java.util.Properties;

public class Config {

    public static final int FILE_INPUT_SLEEP_TIME_MILLIS;

    public static final String[] DISK_NAMES;

    public static final int COUNTER_DATA_LIMIT_CHARS;

    public static final int SORT_PROGRESS_LIMIT_RATE;

    static {
        Properties properties = readPropertiesFile();

        FILE_INPUT_SLEEP_TIME_MILLIS = readIntegerProperty(properties, "file_input_sleep_time");
        DISK_NAMES = properties.getProperty("disks").split(";");
        COUNTER_DATA_LIMIT_CHARS = readIntegerProperty(properties, "counter_data_limit");
        SORT_PROGRESS_LIMIT_RATE = readIntegerProperty(properties, "sort_progress_limit");
    }

    private static Properties readPropertiesFile() {
        String filePath = "src/data/app.properties";
        Properties properties = null;

        try {
            properties = new Properties();
            properties.load(new FileInputStream(filePath));
        } catch (Exception e) {
            System.err.println("File at path " + filePath + " was not found.");
            e.printStackTrace();
        }

        return properties;
    }

    private static Integer readIntegerProperty(Properties properties, String propertyName) {
        int propertyValue = -1;
        try {
            propertyValue = Integer.parseInt(properties.getProperty(propertyName));
        } catch (NumberFormatException nfe) {
            System.err.println("Could not parse " + propertyName + ".");
            System.exit(1);
        }

        return propertyValue;
    }
}