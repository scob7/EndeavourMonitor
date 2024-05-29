/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidapps.endeavour.api;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rapidapps.endeavour.entities.Event;
import com.rapidapps.endeavour.entities.Sensor;
import com.rapidapps.endeavour.sensors.SensorProvider.SensorReading;
import com.rapidapps.endeavour.services.SensorService;

/**
 *
 * @author Dave
 */
@RestController
@RequestMapping(path = RestApi.BASE_PATH)
public class RestApi {

    public static final String BASE_PATH = "/api";

    @Autowired
    private SensorService sensorService;

    // @Context
    // private HttpServletRequest request;
    // final GpioController gpio = GpioFactory.getInstance();
    // @GetMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    // public String test() {
    //     return "hello";
    // }

    @GetMapping(path="/servertime", produces = MediaType.APPLICATION_JSON_VALUE)
    public String serverTime() {
        // Calendar now = Calendar.getInstance();
        return LocalDateTime.now().toString();
        // now.set(Calendar.YEAR, 1969);
        // return DatatypeConverter.printDateTime( now );
    }

    /*
     * 
     * [
     * {id: number, temp:number, serial:string, name:string, min: number, max: number}
     * ]
     * 
     */
    @GetMapping("/sensors")
    public List<Sensor> sensors() {
        //List<AbstractSensorReading> result = new LinkedList<>();

        List<Sensor> sensors = sensorService.findAllSensors();
        Iterator<Sensor> iter = sensors.iterator();
        while (iter.hasNext()) {
            Sensor sensor = iter.next();
            // final JsonObject json = sensor.toJSON();
            try {
                SensorReading reading = sensorService.readSensor(sensor.getSerial());
                if (reading != null){
                    sensor.setTemp( Float.parseFloat(reading.getValue()));
                    //result.add(sensor);
                }

                // if (reading == null) {
                // json.addProperty("temp", "ERR");
                // }
                // else
                // {
                // reading.accept(new AbstractSensor.SensorVisitor() {
                // @Override
                // public void visit(TemperatureSensor sensor) {
                // json.addProperty("temp", sensor.getValue());
                // }

                // @Override
                // public void visit(WaterSensor sensor) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                // }
                // });
                // }
            } catch (IOException ioe) {
                // json.addProperty("temp", "ERR");
            }
            // result.add(json);
        }
        return sensors;
    }

    @GetMapping(path = "/sensor/{id}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Event> events(@PathVariable("id") final long id,
            @RequestParam("start") @DefaultValue("0") long start,
            @RequestParam("count") @DefaultValue("1000") long count,
            @RequestParam("begin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime begin,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        // JsonArray json = new JsonArray();
        // Device device = sensorService.findDeviceById(id);
        // Date beginDate = null;
        // if (begin != null) {
        // beginDate = DatatypeConverter.parseDateTime(begin).getTime();
        // }
        // Date endDate = null;
        // if (end != null) {
        // endDate = DatatypeConverter.parseDateTime(end).getTime();
        // }
        return sensorService.findSensorEvents(id, start, count, begin, end);
    }

    @GetMapping(path = "sensor/{sensorid}/timeseries/{interval}/{timeseries}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Object>  timesseries(
            @PathVariable("sensorid") final long sensorid,
            @PathVariable("interval") int interval,
            @PathVariable("timeseries") String timeseriesParam,
            @RequestParam("begin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime begin,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {

        //OffsetDateTime beginWithOffset = begin.toOffsetDateTime();
        //OffsetDateTime endWithOffset = end.toOffsetDateTime();

        SensorService.TimeSeries timeseries = SensorService.TimeSeries.valueOf(timeseriesParam);
        if (timeseriesParam == null)
            return new ResponseEntity<>("Invalid time interval " + interval, HttpStatus.BAD_REQUEST);

        // Calendar begin = DatatypeConverter.parseDateTime(beginParam);
        // if (begin == null)
        //     throw new BadRequestException("Invalid begin date " + beginParam);

        // Calendar end = DatatypeConverter.parseDateTime(endParam);
        // if (end == null)
        //     throw new BadRequestException("Invalid end date " + endParam);
        List<Object> result = sensorService.queryTemperatureTimeseries(sensorid, begin, end, timeseries, interval);
        return new ResponseEntity<>(result, HttpStatus.OK); 
    }

    @GetMapping(path="/sensors/unregistered", produces =  MediaType.APPLICATION_JSON_VALUE)
    public List<SensorReading> unregisteredSensors() throws IOException {
        return sensorService.findUnregisteredSensors();
    }

    @PostMapping("/sensor/register/{serial}")
    // @Consumes(MediaType.APPLICATION_JSON)
    public Sensor registerSensor(
            @PathVariable("serial") String serial,
            @RequestParam("name") String name,
            @RequestParam("type") int type,
            @RequestParam("min") String minParam,
            @RequestParam("max") String maxParam) {
        Float min = null;
        Float max = null;
        if (minParam != null) {
            min = Float.parseFloat(minParam);
        }
        if (maxParam != null) {
            max = Float.parseFloat(maxParam);
        }
        // AbstractSensor sensor = sensorService.pollSensor(serial);
        // if( sensor == null )
        // throw new NotFoundException("Sensor " + serial + " not found");
        return sensorService.registerSensor(serial.trim(), name.trim(), type, min, max);
    }

    @PutMapping("/sensor/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    public Sensor updateSensor(
            @PathVariable("id") long id,
            @RequestParam("serial") String serial,
            @RequestParam("name") String name,
            @RequestParam("type") int type,
            @RequestParam("min") String minParam,
            @RequestParam("max") String maxParam) {
        Float min = null;
        Float max = null;
        if (minParam != null) {
            min = Float.parseFloat(minParam);
        }
        if (maxParam != null) {
            max = Float.parseFloat(maxParam);
        }

        return sensorService.updateSensor(id, serial.trim(), name.trim(), type, min, max);
    }
}
