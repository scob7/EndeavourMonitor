/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour.sensors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Dave
 */
public class MockSensorProvider implements SensorProvider
{
    
    private List<AbstractSensor> devices;
    
    public MockSensorProvider()
    {
        devices = new ArrayList();
        devices.add( new MockTemperatureSensor("55-ABCDEF", 40, 60)); // engine
        devices.add( new MockTemperatureSensor("2", 15, 30)); // pi
        devices.add( new MockTemperatureSensor("3", -20, 5)); // fridge
        devices.add( new MockTemperatureSensor("4", 30, 40)); // batteries
        //devices.add( new MockDevice("5", "EngineWater", 2 ));
    }
    
    @Override
    public List<AbstractSensor> readSensors()
    {
        // update with new random values
        return devices;
    }

   
    @Override
    public AbstractSensor readSensor(String serial)
    {
        Iterator<AbstractSensor> iter = devices.iterator();
        while( iter.hasNext() )
        {
            AbstractSensor sensor = iter.next();
            if( sensor.getSerial().equalsIgnoreCase(serial))
                return sensor;
        }
        return null;
    }

    public static class MockTemperatureSensor extends TemperatureSensor
    {
        private int min;
        private int max;
        private Random rand = new Random(); 
        
        public MockTemperatureSensor( String id, int min, int max )
        {
            super( id );
            this.min = min;
            this.max = max;
        }

        @Override
        public Float getValue()
        {
            float value = rand.nextFloat() * (max - min) + min;
            return Math.round(value * 100.0f) / 100.0f;
            //return String.format("%.2f", value);
            //return value;
        }
        
    }
}
