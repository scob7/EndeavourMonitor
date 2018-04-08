package ca.endeavour.entities;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Created by Dave on 4/27/2017.
 */
@Entity
@Table(name = "events")
public class Event {

    public static final int DEVICE_TYPE_TEMP = 1;
    public static final int DEVICE_TYPE_WATER = 2;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sensorid")
    private long sensorId;
    //@Column(name = "deviceSerial")
    //private String deviceSerial;
    @Column(name = "devicetype")
    private int deviceType;
    //@Column(name = "deviceName")
    //private String deviceName;
    //@Column(name = "value")
    //private String value;
    @Column(name="temperature")
    private Float temperature;
    //@Column(name = "status")
    //private int status;
    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

//    public String getDeviceSerial() {
//        return deviceSerial;
//    }
//
//    public void setDeviceSerial(String deviceSerial) {
//        this.deviceSerial = deviceSerial;
//    }
//
    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
//
//    public String getDeviceName() {
//        return deviceName;
//    }
//
//    public void setDeviceName(String deviceName) {
//        this.deviceName = deviceName;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }

    public float getTemperature()
    {
        return temperature;
    }
    
    public void setTemperature( float temp )
    {
        this.temperature = temp;
    }
    
//    public int getStatus() {
//        return status;
//    }
//
//    public void setStatus(int status) {
//        this.status = status;
//    }

//    public String getError() {
//        return error;
//    }
//
//    public void setError(String error) {
//        this.error = error;
//    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public JsonObject toJSON()
    {
        JsonObject result = new JsonObject();
        result.addProperty("id", this.id);
        result.addProperty("timestamp", this.timestamp.toString() );
        //result.addProperty("status", this.status );
        if( this.temperature != null )
            result.addProperty("temp",this.temperature);
        return result;
    }

    @Override
    public String toString()
    {
        return this.toJSON().toString();
    }



}
