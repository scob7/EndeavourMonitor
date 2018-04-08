/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour.entities;

import com.google.gson.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Dave
 */
@Entity
@Table(name = "sensors")
public class Sensor {
    
    //public Enum TYPES ={};
    public static final int TEMP = 1;
    public static final int WATER = 2;
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="serial")
    private String serial;
    @Column(name = "name")
    private String name;
    @Column(name="type")
    private int type;
    @Column(name="min")
    private Float min;
    @Column(name="max")
    private Float max;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Float getMin()
    {
        return min;
    }

    public void setMin(Float min)
    {
        this.min = min;
    }

    public Float getMax()
    {
        return max;
    }

    public void setMax(Float max)
    {
        this.max = max;
    }
    
    public JsonObject toJSON()
    {
        JsonObject result = new JsonObject();
        result.addProperty("id", this.getId());
        result.addProperty("serial", this.getSerial());
        result.addProperty("name", this.getName());
        result.addProperty("type", this.getType());
        result.addProperty("min", this.getMin());
        result.addProperty("max", this.getMax());
        return result;
    }
}
