// /*
//  * To change this license header, choose License Headers in Project Properties.
//  * To change this template file, choose Tools | Templates
//  * and open the template in the editor.
//  */
// package com.rapidapps.endeavour.sensors;

// import com.rapidapps.endeavour.entities.Sensor;

// //import com.google.gson.JsonObject;

// /**
//  *
//  * @author Dave
//  */
// public abstract class AbstractSensorReading<T>
// {
//     private String serial;
//     private Sensor sensor;
    
//     public AbstractSensorReading( String serial )
//     {
//         this.serial = serial;
//         this.sensor = null;
//     }
    
//     public AbstractSensorReading( Sensor sensor )
//     {
//         this.serial = sensor.getSerial();
//         this.sensor = sensor;
//     }
    
//     public String getSerial()
//     {
//         return serial;
//     }

//     public Sensor getSensor(){
//         return this.sensor;
//     }
            
//     //public abstract int getType();
//     public abstract T getValue();
//     //public abstract int getType();
    
//     public abstract void accept( SensorVisitor visitor );
    
//     public interface SensorVisitor{
//         public void visit( TemperatureSensor sensor );
//         public void visit( WaterSensor sensor );
//     }
    
//     public abstract class AbstractSensorVisitor implements SensorVisitor{

//         @Override
//         public void visit(TemperatureSensor sensor)
//         {
//         }

//         @Override
//         public void visit(WaterSensor sensor)
//         {
//         }
//     }
    
//     //public abstract JsonObject toJSON();
// }
