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

/**
 *
 * @author Dave
 */
public class W1SensorProvider implements SensorProvider
{
    private W1Master w1 = new W1Master();

    @Override
    public List<AbstractSensor> readSensors()
    {
        List<AbstractSensor> result = new ArrayList<>();
        
        List<W1Device> w1Sensors = w1.getDevices();
        Iterator<W1Device> iter = w1Sensors.iterator();
        while (iter.hasNext())
        {
            W1Device w1Sensor = iter.next();
            try
            {

                result.add(readSensor( w1Sensor ));
            } catch (Exception ex)
            {
                System.err.println("Failed to read sensor " + w1Sensor.getId() + " value.");
                ex.printStackTrace();
                //throw new RuntimeException("Failed to read sensor " + w1Sensor.getId() + " value.", ex );
            }
        }
        
        return result;
    }

    @Override
    public AbstractSensor readSensor(String serial)
    {
        List<W1Device> w1Sensors = w1.getDevices();
        Iterator<W1Device> iter = w1Sensors.iterator();
        while (iter.hasNext())
        {
            W1Device w1Sensor = iter.next();
            if( w1Sensor.getId().equalsIgnoreCase(serial))
            {
                try
                {
                    return readSensor( w1Sensor );
                } catch (Exception ex)
                {
                    System.err.println("Failed to read sensor " + w1Sensor.getId() + " value.");
                    ex.printStackTrace();
                    return null;
                    //throw new RuntimeException("Failed to read sensor " + w1Sensor.getId() + " value.", ex );
                }
            } 
        }
        
        return null;
    }
    
    private static AbstractSensor readSensor( W1Device w1Sensor ) throws IOException
    {
        int type = w1Sensor.getFamilyId();
        // check family id to determine the type of sensor?
        float temp = parseTemperature(w1Sensor);
        TemperatureSensor sensor = new TemperatureSensor(w1Sensor.getId());
        sensor.setValue( temp );
        return sensor;
    }

    public static float parseTemperature(W1Device device) throws IOException
    {
        String value = device.getValue();
        String[] lines = value.split("\n");
        String statusLine = lines[0];
        String[] statusLineValues = statusLine.split(" ");
        String status = statusLineValues[statusLineValues.length - 1];
        if (!status.equals("YES"))
        {
            //return error( w1.getId().trim(),w1.getName().trim(), statusLineValues[statusLineValues.length- 1]);
            throw new IOException("Sensor " + device.getId() + " returned status " + status);
        }

        String valueLine = lines[1];
        String[] valueLineValues = valueLine.split(" ");
        String temp = valueLineValues[valueLineValues.length - 1].substring(2);
        temp = temp.substring(0, 2) + "." + temp.substring(2);
        return Float.parseFloat(temp);
    }
}
