package com.smartcampus.app;

import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;

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
        // More resource classes will be added in Days 3-5
        return classes;
    }
}
