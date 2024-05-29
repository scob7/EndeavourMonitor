/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidapps.endeavour.sensors;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Dave
 */
public interface  SensorProvider
{
    public static class SensorReading{
        private String serial;
        private String value;

        public SensorReading(String serial, String value){
            this.serial = serial;
            this.value = value;
        }

        public String getSerial(){
            return this.serial;
        }

        public String getValue(){
            return this.value;
        }
    }

    public List<String> listSensors();
    public SensorReading readSensor( String serial ) throws IOException;
}
