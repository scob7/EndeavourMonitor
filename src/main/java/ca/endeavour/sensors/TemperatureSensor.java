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
public class TemperatureSensor extends AbstractSensor<Float>
{
    private float temp;
    
    public TemperatureSensor( String serial )
    {
        super( serial );
    }
    
    @Override
    public Float getValue()
    {
         return temp;
    }
    
    public void setValue( float temp )
    {
        this.temp = temp;
    }

    @Override
    public void accept(SensorVisitor visitor)
    {
        visitor.visit(this);
    }
    
    //public abstract float getTemperature();

    @Override
    public JsonObject toJSON()
    {
        JsonObject result = new JsonObject();
        result.addProperty("serial", this.getSerial() );
        result.addProperty("temp", this.getValue() );
        return result;
    }
    
}
