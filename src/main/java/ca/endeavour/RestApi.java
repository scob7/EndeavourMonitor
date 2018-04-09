/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour;

import ca.endeavour.entities.Event;
import ca.endeavour.entities.Sensor;
import ca.endeavour.sensors.AbstractSensor;
import ca.endeavour.sensors.TemperatureSensor;
import ca.endeavour.sensors.WaterSensor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Dave
 */
@Component
@Path("/api")
public class RestApi
{
    @Inject
    private SensorService sensorService;

    @Context
    private HttpServletRequest request;
    //final GpioController gpio = GpioFactory.getInstance();
    
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test()
    {
        return "hello";
    }
    
    @GET
    @Path("sensors")
    @Produces(MediaType.APPLICATION_JSON)
    public String sensors()
    {
        List<Sensor> sensors = sensorService.findAllSensors();
       
        JsonArray result = new JsonArray();
        Iterator<Sensor> iter = sensors.iterator();
        while( iter.hasNext() )
        {
            Sensor sensor = iter.next();
            final JsonObject json = sensor.toJSON();
            AbstractSensor reading = sensorService.pollSensor(sensor.getSerial());
            if( reading == null )
                continue;
            reading.accept(new AbstractSensor.SensorVisitor()
            {
                @Override
                public void visit(TemperatureSensor sensor)
                {
                    json.addProperty("temp", sensor.getValue() );
                }

                @Override
                public void visit(WaterSensor sensor)
                {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            result.add( json );
        }
        return result.toString();
    }

    @GET
    @Path("sensor/{id}/events")
    @Produces(MediaType.APPLICATION_JSON)
    public String events(@PathParam("id") final long id,
            @QueryParam("start") @DefaultValue("0") long start,
            @QueryParam("count") @DefaultValue("1000") long count,
            @QueryParam("begin") String begin,
            @QueryParam("end") String end
            )
    {
        JsonArray json = new JsonArray();
        //Device device = sensorService.findDeviceById(id);
        Date beginDate = null;
        if( begin != null )
           beginDate = DatatypeConverter.parseDateTime(begin).getTime();
        Date endDate = null;
        if( end != null )
            endDate = DatatypeConverter.parseDateTime(end).getTime();
        List<Event> events = sensorService.findSensorEvents( id, start, count, beginDate, endDate);

        Iterator<Event> iter = events.iterator();
        while (iter.hasNext())
        {
            Event event = iter.next();
            //if( event.getId().equals(id))
            json.add(event.toJSON());
        }

        return json.toString();
    }
    
    @GET
    @Path("sensor/{sensorid}/timeseries/{interval}")
    @Produces(MediaType.APPLICATION_JSON)
    public String timesseries(
            @PathParam("sensorid") final long sensorid,
            @PathParam("interval") String interval,
            @QueryParam("begin") String begin,
            @QueryParam("end") String end )
    {
        SensorService.TimeInterval timeInterval = SensorService.TimeInterval.valueOf(interval);
        Date beginDate = DatatypeConverter.parseDateTime(begin).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();
        
        JsonArray result = sensorService.queryTemperatureTimeseries(sensorid, beginDate, endDate, timeInterval);
        return result.toString();
    }

    @GET
    @Path("/sensors/unregistered")
    @Produces(MediaType.APPLICATION_JSON)
    public String unregisteredSensors()
    {
        List<AbstractSensor> sensors = sensorService.findUnregisteredSensors();
        JsonArray result = new JsonArray();
        Iterator<AbstractSensor> iter = sensors.iterator();
        while( iter.hasNext() )
        {
            AbstractSensor sensor = iter.next();
            result.add( sensor.toJSON() );
        }
        
        return result.toString();
    }
    
    @POST
    @Path("/sensor/register/{serial}")
    //@Consumes(MediaType.APPLICATION_JSON)
    public String registerSensor(
            @PathParam("serial") String serial,
            @QueryParam("name") String name,
            @QueryParam("type") int type,
            @QueryParam("min") String minParam,
            @QueryParam("max") String maxParam )
    {
        Float min = null;
        Float max = null;
        if( minParam != null )
            min = Float.parseFloat(minParam);
        if( maxParam != null )
            max = Float.parseFloat(maxParam);
        AbstractSensor sensor = sensorService.pollSensor(serial);
        if( sensor == null )
            throw new NotFoundException("Sensor " + serial + " not found");
        Sensor result = sensorService.registerSensor( sensor, name, type, min, max );
        return result.toJSON().toString();
    }
}