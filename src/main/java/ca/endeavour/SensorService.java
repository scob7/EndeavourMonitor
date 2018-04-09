/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour;

import ca.endeavour.entities.Event;
import ca.endeavour.entities.Sensor;
import ca.endeavour.sensors.AbstractSensor;
import com.pi4j.io.w1.W1Master;
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
import org.springframework.scheduling.TaskScheduler;
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
import javax.persistence.Query;
import javax.persistence.Tuple;
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
    private TaskScheduler scheduler;
    
    @Inject
    private SensorProvider sensorProvider;
    
    private static final Logger log = LoggerFactory.getLogger(SensorService.class);
    
    private final W1Master w1 = new W1Master();
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
    public Sensor registerSensor( String serial, String name, int type, Float min, Float max )
    {
        Sensor result = new Sensor();
        result.setSerial(serial);
        result.setName(name);
        result.setType(type);
        result.setMin( min );
        result.setMax( max );
        em.persist(result);
        return result;
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
    
    public static enum TimeInterval{
        minute, hour, day, week, month, year
    }
    
    public JsonArray queryTemperatureTimeseries( long sensorid, Date begin, Date end, TimeInterval interval )
    {
        SimpleDateFormat postgresFormat = new SimpleDateFormat( "YYYY-MM-dd HH:mm" );
        //String seriesName = series.name().toLowerCase();
        String psBegin = postgresFormat.format(begin);
        String psEnd = postgresFormat.format(end);
        //
       String sql = "SELECT timeseries, temp"//coalesce(temp,0) AS temp"
               + " FROM generate_series( '" + psBegin + "'\\:\\:timestamp, '" + psEnd + "'\\:\\:timestamp, '1 " + interval.name() + "' ) AS timeseries"
               + " LEFT OUTER JOIN (SELECT date_trunc('" + interval.name() + "', timestamp) as interval"
               + " ,avg(temperature) as temp"
               + " FROM events WHERE sensorid = " + sensorid
               + " AND timestamp >= '" + psBegin + "' AND timestamp < '" + psEnd + "'"
               + " GROUP BY interval) results ON (timeseries = results.interval)";
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
           datapoint.addProperty("temp", Math.round(temp * 100) / 100 );
           json.add( datapoint );
       }
       return json;
    }
    
    public AbstractSensor readSensor( String serial ) throws IOException
    {
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

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void pollSensors()
    {
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
