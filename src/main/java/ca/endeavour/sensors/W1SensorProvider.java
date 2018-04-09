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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave
 */
public class W1SensorProvider implements SensorProvider
{
    private W1Master w1 = new W1Master();
    private static final long DEFAULT_WAIT = 200;
    
    private static final Logger log = LoggerFactory.getLogger(W1SensorProvider.class);
    
    @Override
    public List<AbstractSensor> readSensors()
    {
        List<AbstractSensor> result = new ArrayList<>();
        List<W1Device> w1Sensors = w1.getDevices();
        waitForSensors(DEFAULT_WAIT);
        Iterator<W1Device> iter = w1Sensors.iterator();
        while (iter.hasNext())
        {
            W1Device w1Sensor = iter.next();
            try
            {
                result.add(readSensor( w1Sensor ));
            } 
            catch (Exception ex)
            {
                log.warn("Failed to read sensor " + w1Sensor.getId(), ex);
                //System.err.println("Failed to read sensor " + w1Sensor.getId() + " value.");
                //throw new RuntimeException("Failed to read sensor " + w1Sensor.getId() + " value.", ex );
            }
        }
        
        return result;
    }

    private static void waitForSensors( long millis )
    {
        try
        {
            Thread.sleep( millis  );
        }
        catch( InterruptedException ie )
        {
        }
    }
    
    @Override
    public AbstractSensor readSensor( final String serial) throws IOException
    {
        List<W1Device> w1Sensors = w1.getDevices();
        //waitForSensors(DEFAULT_WAIT);
        Iterator<W1Device> iter = w1Sensors.iterator();
        while (iter.hasNext())
        {
            W1Device w1Sensor = iter.next();
            AbstractSensor reading = readSensor( w1Sensor );
            if( w1Sensor.getId().trim().equalsIgnoreCase(serial) )
            {
                return reading;
            } 
        }
        log.warn("Sensor {} not found", serial );
        return null;
    }
    
    private static AbstractSensor readSensor( W1Device w1Sensor ) throws IOException
    {
        int type = w1Sensor.getFamilyId();
        // check family id to determine the type of sensor?
        float temp = parseTemperature(w1Sensor);
        TemperatureSensor sensor = new TemperatureSensor(w1Sensor.getId().trim());
        sensor.setValue( temp );
        return sensor;
    }

    public static float parseTemperature(W1Device device) throws IOException
    {
        int retries = 3;
        while( retries > 0)
        {
            String value = device.getValue();
            log.info( device.getId() + "\n" + value );

            String[] lines = value.split("\n");
            String statusLine = lines[0];
            String[] statusLineValues = statusLine.split(" ");
            String status = statusLineValues[statusLineValues.length - 1];

            if (!status.equals("YES"))
            {
                //return error( w1.getId().trim(),w1.getName().trim(), statusLineValues[statusLineValues.length- 1]);
                //throw new IllegalStateException("Sensor " + device.getId() + " returned status " + status);
                retries--;
                waitForSensors(DEFAULT_WAIT);    
            }

            if( lines.length < 2 )
                throw new IllegalArgumentException("Sensor " + device.getId() + " missing value line");
            String valueLine = lines[1];
            String[] valueLineValues = valueLine.split(" ");
            String temp = valueLineValues[valueLineValues.length - 1];
            if( temp.length() < 3 )
                throw new IllegalArgumentException("Sensor " + device.getId() + " invalid temp:" + temp );
            temp = temp.substring(2);
            if( temp.length() >= 2 )
            {
                temp = temp.substring(0, 2) + "." + temp.substring(2);
            }
            return Float.parseFloat(temp);
        }
        
        throw new IOException("Sensor " + device.getId() + " failed. Too Many retries" );
    }
}
