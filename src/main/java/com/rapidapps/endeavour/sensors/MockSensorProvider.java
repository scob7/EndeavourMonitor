/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidapps.endeavour.sensors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author Dave
 */
public class MockSensorProvider implements SensorProvider
{
    
    private Random rand = new Random(); 
    private Map<String,MockTemperatureSensor> sensors;
    
    public MockSensorProvider()
    {
        sensors = new HashMap<String,MockTemperatureSensor>();
        MockTemperatureSensor s1 = new MockTemperatureSensor("55-ABCDEF", 40, 60);
        MockTemperatureSensor s2 = new MockTemperatureSensor("2", 15, 30);
        MockTemperatureSensor s3 = new MockTemperatureSensor("3", -20, 5);
        MockTemperatureSensor s4 = new MockTemperatureSensor("4", 30, 40);

        sensors.put( s1.getSerial(), s1 ); // engine
        sensors.put( s2.getSerial(), s2 ); // pi
        sensors.put( s3.getSerial(), s3); // fridge
        sensors.put( s4.getSerial(), s4 ); // batteries
        //devices.add( new MockDevice("5", "EngineWater", 2 ));
    }
    
    @Override
    public List<String> listSensors()
    {
        // update with new random values
        return sensors.keySet().stream().collect(Collectors.toUnmodifiableList());
    }

   
    @Override
    public SensorReading readSensor(String serial)
    {
        MockTemperatureSensor sensor = sensors.get(serial);
        int min = sensor.getMin();
        int max = sensor.getMax();

        float value = rand.nextFloat() * (max - min) + min;
        value = Math.round(value * 100.0f) / 100.0f;
        return new SensorReading(serial, Float.toString(value));

    }

    public static class MockTemperatureSensor
    {
        private String serial;
        private int min;
        private int max;
        
        public MockTemperatureSensor( String serial, int min, int max )
        {
            this.serial = serial;
            this.min = min;
            this.max = max;
        }

        public String getSerial(){
            return serial;
        }

        public int getMin(){
            return min;
        }

        public int getMax(){
            return max;
        }

        // @Override
        // public String getValue()
        // {
        //     float value = rand.nextFloat() * (max - min) + min;
        //     value =  Math.round(value * 100.0f) / 100.0f;
        //     return Float.toString(value);
        //     //return String.format("%.2f", value);
        //     //return value;
        // }
        
    }
}
