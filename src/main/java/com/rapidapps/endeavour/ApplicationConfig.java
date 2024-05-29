package com.rapidapps.endeavour;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pi4j.io.w1.W1Master;
import com.rapidapps.endeavour.sensors.MockSensorProvider;
import com.rapidapps.endeavour.sensors.SensorProvider;

@Configuration
@EnableTransactionManagement
@EnableScheduling
public class ApplicationConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer getObjectMapper() {

        return new Jackson2ObjectMapperBuilderCustomizer() {

            @Override
            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
                jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
            }

        };
    }

    @Bean
    public SensorProvider getSensorProvider() {
        return new MockSensorProvider();
    }

    // @Bean
    // RouterFunction<ServerResponse> spaRouter() {
    //     ClassPathResource index = new ClassPathResource("static/index.html");      
    //     return route().resource(path("/"), index).build();
    // }

}
