/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidapps.endeavour.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rapidapps.endeavour.entities.Event;
import com.rapidapps.endeavour.entities.Sensor;
import com.rapidapps.endeavour.sensors.SensorProvider;
import com.rapidapps.endeavour.sensors.SensorProvider.SensorReading;

/**
 *
 * @author Dave
 */
@Repository
@Transactional
@Service
public class SensorService {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private SensorProvider sensorProvider;

    private static final Logger log = LoggerFactory.getLogger(SensorService.class);

    // @Inject
    private final Map<String, Sensor> sensorCache = new ConcurrentHashMap<>();
    private final Map<String, SensorReading> sensorReadings = new ConcurrentHashMap<>();

    // public static Event success( Device device, String value )
    // {
    // Event event = new Event();
    // event.setDeviceId( deviceId );
    // event.deviceName = deviceName;
    // event.value = value;
    // event.status = STATUS_OK;
    // event.timestamp = new Date();
    // return event;
    // }
    //
    // public static Event error( String deviceId, String deviceName, String error )
    // {
    // Event event = new Event();
    // event.deviceId = deviceId;
    // event.deviceName = deviceName;
    // event.error = error;
    // event.status = STATUS_ERROR;
    // event.timestamp = new Date();
    // return event;
    // }
    private Event saveReading(Sensor sensor, SensorReading reading) {
        Event event = new Event();
        event.setSensorId(sensor.getId());
        event.setTimestamp(OffsetDateTime.now());
        event.setTemperature( Float.parseFloat(reading.getValue()) );
        event.setDeviceType(Event.DEVICE_TYPE_TEMP);
        // sensor.accept(new AbstractSensorReading.SensorVisitor() {
        //     @Override
        //     public void visit(TemperatureSensor sensor) {
        //         event.setDeviceType(Event.DEVICE_TYPE_TEMP);
        //         event.setTemperature(sensor.getValue());
        //     }

        //     @Override
        //     public void visit(WaterSensor sensor) {
        //         throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
        //                                                                        // choose Tools | Templates.
        //     }
        // });
        em.persist(event);
        sensorReadings.put(sensor.getSerial(), reading);
        return event;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Sensor updateSensor(long id, String serial, String name, int type, Float min, Float max) {
        // CriteriaBuilder cb = em.getCriteriaBuilder();
        // cb.equal( Sensor_., cb)
        // em.createQuery( Sensor.class, serial );
        // TypedQuery<Sensor> query = em.createQuery("SELECT Sensor s WHERE s.serial =
        // :serial", Sensor.class );
        // query.setParameter("serial", serial);
        Sensor sensor = em.find(Sensor.class, id);// query.getSingleResult();
        sensor.setSerial(serial);
        sensor.setType(type);
        sensor.setName(name);
        sensor.setMin(min);
        sensor.setMax(max);
        sensor = em.merge(sensor);

        sensorCache.put(serial, sensor);
        return sensor;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Sensor registerSensor(String serial, String name, int type, Float min, Float max) {

        // em.createQuery( Sensor.class, serial );
        Sensor sensor = new Sensor();
        sensor.setSerial(serial);
        sensor.setName(name);
        sensor.setType(type);
        sensor.setMin(min);
        sensor.setMax(max);
        em.persist(sensor);

        sensorCache.put(serial, sensor);
        return sensor;
    }

    @Transactional(readOnly = true)
    public Sensor findDeviceByName(String name) {
        // CriteriaBuilder cb = em.getCriteriaBuilder();
        // cb.createQuery(Device.class);
        TypedQuery<Sensor> query = em.createQuery("SELECT d FROM Device d WHERE LOWER(d.name) = LOWER(:name)",
                Sensor.class);
        query.setParameter("name", name);
        List<Sensor> result = query.getResultList();
        if (result.size() > 0) {
            return result.get(0);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public Sensor findSensorBySerial(String serial) {
        // CriteriaBuilder cb = em.getCriteriaBuilder();
        // cb.createQuery(Device.class);
        TypedQuery<Sensor> query = em.createQuery("SELECT s FROM Sensor s WHERE LOWER(s.serial) = LOWER(:serial)",
                Sensor.class);
        query.setParameter("serial", serial);
        List<Sensor> result = query.getResultList();
        if (result.size() > 0) {
            return result.get(0);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public Sensor findSensorById(long id) {
        return em.find(Sensor.class, id);
    }

    @Transactional(readOnly = true)
    public List<Sensor> findAllSensors() {
        TypedQuery<Sensor> query = em.createQuery("SELECT s from Sensor s ORDER BY s.id ASC", Sensor.class);
        return query.getResultList();
    }

    // @Transactional(readOnly = true)
    // public List<Event> findSensorEvents(long sensorId, long start, long max, LocalDateTime begin, LocalDateTime end) {
    //     String hql = "SELECT e from Event e WHERE e.sensorId = :sensorId";
    //     if (begin != null)
    //         hql += " AND e.timestamp >= :begin";
    //     if (end != null)
    //         hql += " AND e.timestamp <= :end";
    //     hql += " ORDER BY e.timestamp ASC";
    //     TypedQuery<Event> query = em.createQuery(hql, Event.class);
    //     query.setParameter("sensorId", sensorId);
    //     if (begin != null)
    //         query.setParameter("begin", begin);
    //     if (end != null)
    //         query.setParameter("end", end);

    //     return query.getResultList();
    // }

    public static enum TimeSeries {
        minute, hour, day, week, month, year
    }

    public static class TempReading {

        public LocalDateTime timestamp;
        public float temp;

        public TempReading(LocalDateTime timestamp, float temp){
            this.timestamp = timestamp;
            this.temp = temp;
        }
    }

    public List<Object> queryTemperatureTimeseries(long sensorid, OffsetDateTime begin, OffsetDateTime end,
            TimeSeries timeseries, int interval) {
        begin = begin.withNano(0);
        end = end.withNano(0);

        // Calendar cbegin = Calendar.getInstance();
        // cbegin.setTime(begin);
        // cbegin.set( Calendar.MILLISECOND, 0 );

        // Calendar cend = Calendar.getInstance();
        // cend.setTime(end);
        // cend.set( Calendar.MILLISECOND, 0);

        if (timeseries.ordinal() >= TimeSeries.minute.ordinal()) {
            begin = begin.withSecond(0);
            end = end.withSecond(0);
        }

        if (timeseries.ordinal() >= TimeSeries.hour.ordinal()) {
            // cbegin.set( Calendar.MINUTE, cbegin.getActualMinimum( Calendar.MINUTE ));
            // cend.set( Calendar.MINUTE, cend.getActualMaximum( Calendar.HOUR_OF_DAY));
            begin = begin.withMinute(0);
            end = end.withMinute(59);
        }

        if (timeseries.ordinal() >= TimeSeries.day.ordinal()) {
            // cbegin.set( Calendar.HOUR_OF_DAY, cbegin.getActualMinimum(
            // Calendar.HOUR_OF_DAY ));
            // cend.set( Calendar.HOUR_OF_DAY, cend.getActualMaximum( Calendar.HOUR_OF_DAY)
            // );
            begin = begin.withHour(0);
            end = end.withHour(23);
        }

        if (timeseries.ordinal() >= TimeSeries.month.ordinal()) {
            // cbegin.set( Calendar.DATE, cbegin.getActualMinimum( Calendar.DATE ));
            // cend.set( Calendar.DATE, cend.getActualMaximum( Calendar.DATE));
            begin = begin.withDayOfMonth(1);
            end = end.withDayOfMonth(YearMonth.of(end.getYear(), end.getMonth()).lengthOfMonth());
        }

        if (timeseries.ordinal() >= TimeSeries.year.ordinal()) {
            // cbegin.set( Calendar.MONTH, cbegin.getActualMinimum(Calendar.MONTH ));
            // cend.set( Calendar.MONTH, cend.getActualMaximum( Calendar.MONTH));
            begin = begin.withMonth(1);
            end = end.withMonth(12);
        }

        // SimpleDateFormat postgresFormat = new SimpleDateFormat( "YYYY-MM-dd HH:mm" );
        DateTimeFormatter psDateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss Z");
        // String seriesName = series.name().toLowerCase();

        String psBegin = begin.atZoneSameInstant(ZoneId.systemDefault()).format(psDateFormatter);
        String psEnd = end.atZoneSameInstant(ZoneId.systemDefault()).format(psDateFormatter);
        //
        String sql = "SELECT timeseries, temp "// coalesce(temp,0) AS temp"
                + " FROM generate_series( '" + psBegin + "'\\:\\:TIMESTAMP WITH TIME ZONE, '" + psEnd + "'\\:\\:TIMESTAMP WITH TIME ZONE, '"
                + interval + " " + timeseries.name() + "' ) AS timeseries"
                + " LEFT OUTER JOIN (SELECT date_trunc('" + timeseries.name() + "', timestamp) as interval"
                + " , avg(temperature) as temp"
                + " FROM events WHERE sensorid = " + sensorid
                + " AND timestamp >= '" + psBegin + "' AND timestamp < '" + psEnd + "'"
                + " GROUP BY interval) results ON (timeseries = results.interval)";
        log.info("Query timeseries for sensor {}\n{}", sensorid, sql);
        Query query = em.createNativeQuery(sql);
        // query.setParameter("begin", begin );
        // query.setParameter("end", end );
        // query.setParameter("sensorid", sensorid );
        // query.setParameter("oneinterval", "1 " + interval.name() );
        // query.setParameter("interval", interval.name() );

        List<Object> result = query.getResultList();

        // JsonArray json = new JsonArray();
        // Iterator<Object[]> iter = result.iterator();
        // while( iter.hasNext() )
        // {
        // Object[] row = iter.next();
        // LocalDateTime timestamp = (LocalDateTime)row[0];
        // Double temp = (Double)row[1];
        // JsonObject datapoint = new JsonObject();
        // datapoint.addProperty("timestamp", timestamp.toString() );
        // if( temp != null )
        // temp = Math.round(temp * 100.0) / 100.0;
        // datapoint.addProperty("temp", temp );
        // json.add( datapoint );
        // }
        // return json;
        return result;
    }

    public SensorReading getLatestSensorReading(String serial) throws IOException {
        //sensorCache.get(serial).getTemp();
        SensorReading cachedReading = sensorReadings.get(serial);
        return cachedReading;
    }

    private void cacheSensors() {
        sensorCache.clear();
        List<Sensor> sensors = findAllSensors();
        Iterator<Sensor> iter = sensors.iterator();
        while (iter.hasNext()) {
            Sensor sensor = iter.next();
            sensorCache.put(sensor.getSerial(), sensor);
            //sensorReadings.put(sensor.getSerial(), readSensor(sensor.getSerial()));
        }
    }


    @Transactional(readOnly = true)
    public List<SensorReading> findUnregisteredSensors() throws IOException {
        this.cacheSensors();
        List<SensorReading> result = new ArrayList<>();
        List<String> sensors = sensorProvider.listSensors();
        Iterator<String> iter = sensors.iterator();
        while (iter.hasNext()) {
            String serial = iter.next();
            if (!sensorCache.containsKey(serial)){
                result.add(sensorProvider.readSensor(serial));
            }
        }
        return result;
    }

    // private static final int SENSOR_READ_RATE = 1000 * 60; // 1 minute in millis
    // @Scheduled(fixedRate=SENSOR_READ_RATE)
    @Scheduled(cron = "30 0/1 * 1/1 * *")
    @Transactional
    public void pollSensors() {
        log.info("Starting to poll sensors...");

        //List<Sensor> sensors = this.findAllSensors();
        //List<String> sensors = sensorProvider.listSensors();
        Iterator<Sensor> iter = sensorCache.values().iterator();
        while (iter.hasNext()) {
            Sensor sensor = iter.next();
            String serial = sensor.getSerial();
            //Sensor sensorEntity = sensorCache.get(serial);
            // if (sensorEntity == null) {
            //     sensorEntity = findSensorBySerial(serial);
            //     if (sensorEntity == null) {
            //         // log warn that sensor is not configured
            //         log.warn("Unconfigured sensor: {}", serial);
            //         continue;
            //     }
            //     sensorCache.put(serial, sensorEntity);
            // }

            try {
                SensorReading reading = sensorProvider.readSensor(serial);
                if( reading != null )
                    saveReading(sensor, reading );
            } catch (Exception e) {
                log.warn("Failed to poll sensor " + serial, e);
            }
        }
        log.info("Finished polling sensors...");
    }

    @Scheduled(fixedDelay = 24, timeUnit =TimeUnit.HOURS)
    @Transactional
    public void cleanup() {
        OffsetDateTime cleanupDate = OffsetDateTime.now().minusMonths(6);

        log.info("Delete old data before ", cleanupDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // create delete
        CriteriaDelete<Event> delete = cb.createCriteriaDelete(Event.class);

        // set the root class
        Root<Event> e = delete.from(Event.class);

        // set where clause
        
        delete.where(cb.lessThanOrEqualTo(e.get("timestamp"), cleanupDate ));

        // perform update
        em.createQuery(delete).executeUpdate();
    }

    @PostConstruct
    @Transactional
    public void init() {
        System.out.println("Sensor service init()");
        cacheSensors();
        pollSensors();
    }

}
