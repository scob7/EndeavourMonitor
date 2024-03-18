/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour;

import ca.endeavour.entities.Event;
import ca.endeavour.entities.Sensor;
import ca.endeavour.sensors.AbstractSensor;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ca.endeavour.sensors.SensorProvider;
import ca.endeavour.sensors.TemperatureSensor;
import ca.endeavour.sensors.WaterSensor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Calendar;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Dave
 */
@Repository
@Transactional
@Service
public class SensorService
{
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private SensorProvider sensorProvider;
    
    private static final Logger log = LoggerFactory.getLogger(SensorService.class);
    
    //@Inject
    private final Map<String,Sensor> sensorCache = new ConcurrentHashMap<>();
   

//    public static Event success( Device device, String value )
//    {
//        Event event = new Event();
//        event.setDeviceId( deviceId );
//        event.deviceName = deviceName;
//        event.value = value;
//        event.status = STATUS_OK;
//        event.timestamp = new Date();
//        return event;
//    }
//
//    public static Event error( String deviceId, String deviceName, String error )
//    {
//        Event event = new Event();
//        event.deviceId = deviceId;
//        event.deviceName = deviceName;
//        event.error = error;
//        event.status = STATUS_ERROR;
//        event.timestamp = new Date();
//        return event;
//    }
    public Event log( Sensor entity, AbstractSensor sensor )
    {
        Event event = new Event();
        event.setSensorId(entity.getId());
        
        event.setTimestamp( new Date() );
        sensor.accept( new AbstractSensor.SensorVisitor()
        {
            @Override
            public void visit(TemperatureSensor sensor)
            {
                event.setDeviceType( Event.DEVICE_TYPE_TEMP);
                event.setTemperature( sensor.getValue() );
            }

            @Override
            public void visit(WaterSensor sensor)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        em.persist(event);
        return event;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Sensor updateSensor( long id, String serial, String name, int type, Float min, Float max )
    {
        //CriteriaBuilder cb = em.getCriteriaBuilder();
        //cb.equal( Sensor_., cb)
        //em.createQuery( Sensor.class, serial );
        //TypedQuery<Sensor> query = em.createQuery("SELECT Sensor s WHERE s.serial = :serial", Sensor.class );
        //query.setParameter("serial", serial);
        Sensor sensor = em.find(Sensor.class, id);//query.getSingleResult();
        sensor.setSerial( serial );
        sensor.setType( type );
        sensor.setName( name );
        sensor.setMin( min );
        sensor.setMax( max );
        em.merge(sensor);
        
        sensorCache.put(serial, sensor);
        return sensor;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Sensor registerSensor( String serial, String name, int type, Float min, Float max )
    {
        
        //em.createQuery( Sensor.class, serial );
        Sensor sensor = new Sensor();
        sensor.setSerial(serial);
        sensor.setName(name);
        sensor.setType(type);
        sensor.setMin( min );
        sensor.setMax( max );
        em.persist(sensor);
        
        sensorCache.put(serial, sensor);
        return sensor;
    }

    @Transactional(readOnly = true)
    public Sensor findDeviceByName(String name)
    {
        //CriteriaBuilder cb = em.getCriteriaBuilder();
        //cb.createQuery(Device.class);
        TypedQuery<Sensor> query = em.createQuery("SELECT d FROM Device d WHERE LOWER(d.name) = LOWER(:name)", Sensor.class);
        query.setParameter("name", name);
        List<Sensor> result = query.getResultList();
        if (result.size() > 0)
        {
            return result.get(0);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public Sensor findSensorBySerial(String serial)
    {
        //CriteriaBuilder cb = em.getCriteriaBuilder();
        //cb.createQuery(Device.class);
        TypedQuery<Sensor> query = em.createQuery("SELECT s FROM Sensor s WHERE LOWER(s.serial) = LOWER(:serial)", Sensor.class);
        query.setParameter("serial", serial);
        List<Sensor> result = query.getResultList();
        if (result.size() > 0)
        {
            return result.get(0);
        }

        return null;
    }

    @Transactional(readOnly = true)
    public Sensor findSensorById(long id)
    {
        return em.find(Sensor.class, id);
    }

    @Transactional(readOnly = true)
    public List<Sensor> findAllSensors()
    {
        TypedQuery<Sensor> query = em.createQuery("SELECT s from Sensor s ORDER BY s.id ASC", Sensor.class);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<Event> findSensorEvents(long sensorId, long start, long max, Date begin, Date end)
    {
        String hql = "SELECT e from Event e WHERE e.sensorId = :sensorId";
        if( begin != null )
            hql += " AND e.timestamp >= :begin";
        if( end != null )
            hql += " AND e.timestamp <= :end";
        hql += " ORDER BY e.timestamp ASC";
        TypedQuery<Event> query = em.createQuery( hql , Event.class);
        query.setParameter("sensorId", sensorId);
        if( begin != null )
            query.setParameter("begin", begin);
        if( end != null )
            query.setParameter("end", end);
        
        return query.getResultList();
    }
    
    public static enum TimeSeries{
        minute, hour, day, week, month, year
    }
    
    public JsonArray queryTemperatureTimeseries( long sensorid, Date begin, Date end, TimeSeries timeseries, int interval )
    {
        Calendar cbegin = Calendar.getInstance();
        cbegin.setTime(begin);
        cbegin.set( Calendar.MILLISECOND, 0 );
        Calendar cend = Calendar.getInstance();
        cend.setTime(end);
        cend.set( Calendar.MILLISECOND, 0);
        
        if( timeseries.ordinal() >= TimeSeries.minute.ordinal() )
        {
            cbegin.set( Calendar.SECOND, 0);
            cend.set( Calendar.SECOND, 0);
        }  
        
        if( timeseries.ordinal() >= TimeSeries.hour.ordinal() )
        {
            cbegin.set( Calendar.MINUTE, cbegin.getActualMinimum( Calendar.MINUTE ));
            cend.set( Calendar.MINUTE, cend.getActualMaximum( Calendar.HOUR_OF_DAY));
        }
        
        if( timeseries.ordinal() >= TimeSeries.day.ordinal() )
        {
            cbegin.set( Calendar.HOUR_OF_DAY, cbegin.getActualMinimum( Calendar.HOUR_OF_DAY ));
            cend.set( Calendar.HOUR_OF_DAY, cend.getActualMaximum( Calendar.HOUR_OF_DAY) );
        }
        
        if( timeseries.ordinal() >= TimeSeries.month.ordinal() )
        {
            cbegin.set( Calendar.DATE, cbegin.getActualMinimum( Calendar.DATE ));
            cend.set( Calendar.DATE, cend.getActualMaximum( Calendar.DATE));
        }
        
        if( timeseries.ordinal() >= TimeSeries.year.ordinal() )
        {
            cbegin.set( Calendar.MONTH, cbegin.getActualMinimum(Calendar.MONTH ));
            cend.set( Calendar.MONTH, cend.getActualMaximum( Calendar.MONTH));
        }
        
        
        SimpleDateFormat postgresFormat = new SimpleDateFormat( "YYYY-MM-dd HH:mm" );
        //String seriesName = series.name().toLowerCase();
        String psBegin = postgresFormat.format(cbegin.getTime());
        String psEnd = postgresFormat.format(cend.getTime());
        //
       String sql = "SELECT timeseries, temp AS temp"//coalesce(temp,0) AS temp"
               + " FROM generate_series( '" + psBegin + "'\\:\\:timestamp, '" + psEnd + "'\\:\\:timestamp, '" + interval + " " + timeseries.name() + "' ) AS timeseries"
               + " LEFT OUTER JOIN (SELECT date_trunc('" + timeseries.name() + "', timestamp) as interval"
               + " , avg(temperature) as temp"
               + " FROM events WHERE sensorid = " + sensorid
               + " AND timestamp >= '" + psBegin + "' AND timestamp < '" + psEnd + "'"
               + " GROUP BY interval) results ON (timeseries = results.interval)";
       log.info("Query timeseries for sensor {}\n{}", sensorid, sql );
       Query query = em.createNativeQuery(sql);
       //query.setParameter("begin", begin );
       //query.setParameter("end", end );
       //query.setParameter("sensorid", sensorid );
       //query.setParameter("oneinterval", "1 " + interval.name() );
       //query.setParameter("interval", interval.name() );
       
       List<Object[]> result = query.getResultList();
       
       JsonArray json = new JsonArray();
       Iterator<Object[]> iter = result.iterator();
       while( iter.hasNext() )
       {
           Object[] row = iter.next();
           Date timestamp = (Date)row[0];
           Double temp = (Double)row[1];
           JsonObject datapoint = new JsonObject();
           datapoint.addProperty("timestamp", timestamp.toString() );
           if( temp != null )
               temp = Math.round(temp * 100.0) / 100.0;
           datapoint.addProperty("temp", temp );
           json.add( datapoint );
       }
       return json;
    }
    
    public AbstractSensor readSensor( String serial ) throws IOException
    {
        //sensorProvider.readSensors();
        return sensorProvider.readSensor(serial);
    }

    private void cacheSensors()
    {
        sensorCache.clear();
        List<Sensor> sensors = findAllSensors();
        Iterator<Sensor> iter = sensors.iterator();
        while (iter.hasNext())
        {
            Sensor sensor = iter.next();
            sensorCache.put(sensor.getSerial(), sensor);
        }
    }
    
    @Transactional(readOnly = true)
    public List<AbstractSensor> findUnregisteredSensors()
    {
        List<AbstractSensor> result = new ArrayList<>();
        List<AbstractSensor> sensors = sensorProvider.readSensors();
        Iterator<AbstractSensor> iter = sensors.iterator();
        while( iter.hasNext() )
        {
            AbstractSensor sensor = iter.next();
            if( !sensorCache.containsKey( sensor.getSerial() ))
                result.add(sensor);
        }
        return result;
    }
    
    private static final int SENSOR_READ_RATE = 1000 * 60; // 1 minute in millis
    //@Scheduled(fixedRate=SENSOR_READ_RATE)
    @Scheduled(cron = "30 0/1 * 1/1 * *")
    @Transactional
    public void pollSensors()
    {
        log.info("Starting to poll sensors...");
        List<AbstractSensor> sensors = sensorProvider.readSensors();
        Iterator<AbstractSensor> iter = sensors.iterator();
        while (iter.hasNext())
        {
            AbstractSensor sensor = iter.next();
            String serial = sensor.getSerial();
            Sensor sensorEntity = sensorCache.get(serial);
            if (sensorEntity == null)
            {
                sensorEntity = findSensorBySerial(serial);
                if (sensorEntity == null)
                {
                    // log warn that sensor is not configured
                    log.warn("Unconfigured sensor: {}", serial);
                    continue;
                }
                sensorCache.put(serial, sensorEntity);
            }

            log(sensorEntity, sensor);
        }
        log.info("Finished polling sensors...");
    }

    @Scheduled(cron = "0 0 12 1/5 * ?")
    @Transactional
    public void cleanup()
    {
        // TODO delete data older than 30 days old
        LocalDateTime cutoff = LocalDateTime.now().minus(Period.ofDays(30));
        
        String hql = "DELETE FROM Event e WHERE e.timestamp < :cutoff";
        TypedQuery<Event> query = em.createQuery( hql , Event.class);
        query.setParameter("cutoff", cutoff);
        query.executeUpdate();
    }

    //@PostInitialize
    @Transactional
    public void init()
    {
        System.out.println("Sensor service init()");
        cacheSensors();
        pollSensors();
    }

}
