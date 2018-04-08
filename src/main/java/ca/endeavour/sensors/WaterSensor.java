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
public class WaterSensor extends AbstractSensor<Boolean>
{
    private boolean inWater;
    
    public WaterSensor( String serial, boolean inWater )
    {
        super( serial );
        this.inWater = inWater;
    }
    
    @Override
    public void accept(SensorVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public Boolean getValue()
    {
        return inWater;
    }
    
    @Override
    public JsonObject toJSON()
    {
        JsonObject result = new JsonObject();
        result.addProperty("serial", this.getSerial() );
        result.addProperty("water", this.getValue() );
        return result;
    }
}
