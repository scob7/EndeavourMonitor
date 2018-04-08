/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.endeavour;

import ca.endeavour.sensors.MockSensorProvider;
import ca.endeavour.sensors.SensorProvider;
import ca.endeavour.sensors.W1SensorProvider;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Dave
 */
@Configuration
@EnableTransactionManagement
@ComponentScan//(basePackageClasses = SpringConfig.class )
@EnableScheduling
public class SpringConfig
{

//    @Bean
//    public DataSource dataSource()
//    {
//        DriverManagerDataSource driver = new DriverManagerDataSource();
//        driver.setDriverClassName("org.postgresql.Driver");
//        driver.setUrl("jdbc:postgresql://localhost:5432/sensors");
//        driver.setUsername("sensors");
//        driver.setPassword("Endeavour");
//        return driver;
//    }
    
    @Bean
    public DataSource dataSource()
    {
        DriverManagerDataSource driver = new DriverManagerDataSource();
        //driver.setDriverClassName("com.mysql.cj.jdbc.Driver");
        driver.setDriverClassName(org.postgresql.Driver.class.getName());
        //driver.setUrl("jdbc:mysql://localhost:3306/sensors");
        driver.setUrl("jdbc:postgresql://localhost:5432/sensors");
        driver.setUsername("sensors");
        driver.setPassword("Endeavour");
        return driver;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
    {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.POSTGRESQL);
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(false);
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(getClass().getPackage().getName());
        factory.setDataSource(dataSource());

        return factory;
    }

    @Bean
    public JpaTransactionManager transactionManager()
    {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }

    @Bean
    public TaskScheduler scheduler()
    {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        return scheduler;
    }
    
    @Bean
    public SensorProvider sensorProvider()
    {
        String os = System.getProperty("os.name");
        if( os.toLowerCase().contains("win") )
            return new MockSensorProvider();
        else
            return new W1SensorProvider();
    }
}
