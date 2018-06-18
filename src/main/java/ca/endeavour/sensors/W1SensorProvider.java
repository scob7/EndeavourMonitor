/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour.sensors;

import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave
 */
public class W1SensorProvider implements SensorProvider {

    private W1Master w1 = new W1Master();
    //private static final long DEFAULT_WAIT = 200;

    private static final Logger log = LoggerFactory.getLogger(W1SensorProvider.class);

    private static long LAST_READ_ATTEMPT = 0;
    private static final long READ_DELAY = 100; // delay reads by 10 ms

    @Override
    public List<AbstractSensor> readSensors() {
        List<AbstractSensor> result = new ArrayList<>();
        List<W1Device> w1Sensors = w1.getDevices();
        //waitForSensors(DEFAULT_WAIT);
        Iterator<W1Device> iter = w1Sensors.iterator();
        while (iter.hasNext()) {
            W1Device w1Sensor = iter.next();
            try {
                result.add(readSensor(w1Sensor));
            } catch (Exception ex) {
                log.warn("Failed to read sensor " + w1Sensor.getId(), ex);
                //System.err.println("Failed to read sensor " + w1Sensor.getId() + " value.");
                //throw new RuntimeException("Failed to read sensor " + w1Sensor.getId() + " value.", ex );
            }
        }

        return result;
    }

    private static void waitForSensors(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
        }
    }

    @Override
    public AbstractSensor readSensor(final String serial) throws IOException {
        List<W1Device> w1Sensors = w1.getDevices();
        //waitForSensors(DEFAULT_WAIT);
        Iterator<W1Device> iter = w1Sensors.iterator();
        while (iter.hasNext()) {
            W1Device w1Sensor = iter.next();
            if (w1Sensor.getId().trim().equalsIgnoreCase(serial)) {
                return readSensor(w1Sensor);
            }
        }
        log.warn("Sensor {} not found", serial);
        return null;
    }

    private static AbstractSensor readSensor(W1Device w1Sensor) throws IOException {
        //int type = w1Sensor.getFamilyId();
        // check family id to determine the type of sensor?
        final String id = w1Sensor.getId().trim();
        int retries = 3;
        while (retries > 0) {
            retries--;
            long time = System.currentTimeMillis();
            //long delay = time - LAST_READ_ATTEMPT;
            long delay = time - LAST_READ_ATTEMPT - READ_DELAY;
            if (LAST_READ_ATTEMPT > 0 && delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    throw new IOException( ex );
                }
            }

            LAST_READ_ATTEMPT = time;
            try {
                String value = w1Sensor.getValue();
                log.info(w1Sensor.getId() + "\n" + value);
                float temp = parseTemperature(value);
                TemperatureSensor sensor = new TemperatureSensor(id);
                sensor.setValue(temp);
                return sensor;
            } catch (Exception ex) {
                log.warn("Failed to read sensor " + id, ex);   
            }
        }

        throw new IOException("Sensor " + id + " failed. Too Many retries");
    }

    public static float parseTemperature(final String value) throws Exception {
        String[] lines = value.split("\n");
        String statusLine = lines[0];
        String[] statusLineValues = statusLine.split(" ");
        String status = statusLineValues[statusLineValues.length - 1];
        int crc = Integer.parseInt(statusLineValues[statusLineValues.length - 2].split("=")[1], 16);

        if (crc == 0 || !status.equals("YES")) {
            //return error( w1.getId().trim(),w1.getName().trim(), statusLineValues[statusLineValues.length- 1]);
            throw new IOException("Status " + status);
//            retries--;
//            waitForSensors(DEFAULT_WAIT);
//            continue;
        }

        if (lines.length < 2) {
            throw new IOException( "Missing value line");
        }
        String valueLine = lines[1];
        String[] valueLineValues = valueLine.split(" ");
        String temp = valueLineValues[valueLineValues.length - 1];
        if (temp.length() < 3) {
            throw new IOException("Invalid temp: " + temp);
        }
        temp = temp.substring(2);
        if (temp.length() >= 2) {
            temp = temp.substring(0, 2) + "." + temp.substring(2);
        }
        float result = (float)round( Double.parseDouble(temp), 10 );
        return result;
    }

    public static double round( double value, int scale) {
        double pow = Math.pow(10, scale);
        return Math.round(value * pow) / pow;
    }
}
