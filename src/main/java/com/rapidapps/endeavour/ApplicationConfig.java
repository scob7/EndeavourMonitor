package com.rapidapps.endeavour;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rapidapps.endeavour.sensors.MockSensorProvider;
import com.rapidapps.endeavour.sensors.SensorProvider;
import com.rapidapps.endeavour.sensors.W1SensorProvider;

@Configuration
@EnableTransactionManagement
@EnableScheduling
public class ApplicationConfig {

    @Value("${w1.mock:false}")
    private Boolean mock1w;

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
        if( mock1w )
            return new MockSensorProvider();
        return new W1SensorProvider();
        
    }

    // @Bean
    // RouterFunction<ServerResponse> spaRouter() {
    //     ClassPathResource index = new ClassPathResource("static/index.html");      
    //     return route().resource(path("/"), index).build();
    // }

}
