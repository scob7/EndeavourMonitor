/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour.sensors;

import com.google.gson.JsonObject;

/**
 *
 * @author Dave
 */
public abstract class AbstractSensor<T>
{
    private String serial;
    
    public AbstractSensor( String serial )
    {
        this.serial = serial;
    }
    
    public String getSerial()
    {
        return serial;
    }
            
    //public abstract int getType();
    public abstract T getValue();
    //public abstract int getType();
    
    public abstract void accept( SensorVisitor visitor );
    
    public interface SensorVisitor{
        public void visit( TemperatureSensor sensor );
        public void visit( WaterSensor sensor );
    }
    
    public abstract class AbstractSensorVisitor implements SensorVisitor{

        @Override
        public void visit(TemperatureSensor sensor)
        {
        }

        @Override
        public void visit(WaterSensor sensor)
        {
        }
    }
    
    public abstract JsonObject toJSON();
}
