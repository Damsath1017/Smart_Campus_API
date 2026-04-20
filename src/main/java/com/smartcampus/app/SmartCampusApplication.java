package com.smartcampus.app;

import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.exception.mappers.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.mappers.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.mappers.SensorUnavailableExceptionMapper;
import com.smartcampus.exception.mappers.GlobalExceptionMapper;
import com.smartcampus.filter.LoggingFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application entry point.
 * @ApplicationPath defines the base URI for all REST resources.
 * The actual effective path is also governed by the web.xml servlet mapping (/api/v1/*).
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Day 1: Discovery endpoint
        classes.add(DiscoveryResource.class);
        // Day 2: Room management
        classes.add(RoomResource.class);
        // Day 3: Sensor management
        classes.add(SensorResource.class);
        
        // Day 5: Exception Mappers & Logging Filter
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(LoggingFilter.class);

        return classes;
    }
}
