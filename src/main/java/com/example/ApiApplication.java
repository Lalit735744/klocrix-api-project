package com.example;

import com.example.api.AuthResource;
import com.example.api.JwtAuthFilter;
import com.example.api.UserResource;
import com.example.api.ContactResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class ApiApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(AuthResource.class);
        resources.add(UserResource.class); // Add other resources as needed
        resources.add(JwtAuthFilter.class);
        resources.add(ContactResource.class);
        return resources;
    }
}
